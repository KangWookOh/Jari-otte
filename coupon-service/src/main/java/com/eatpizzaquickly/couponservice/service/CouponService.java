package com.eatpizzaquickly.couponservice.service;

import com.eatpizzaquickly.couponservice.client.ApiResponse;
import com.eatpizzaquickly.couponservice.client.UserClient;
import com.eatpizzaquickly.couponservice.common.config.UserCouponsChangedEvent;
import com.eatpizzaquickly.couponservice.dto.CouponRequestDto;
import com.eatpizzaquickly.couponservice.dto.CouponResponseDto;
import com.eatpizzaquickly.couponservice.dto.UserResponseDto;
import com.eatpizzaquickly.couponservice.entity.Coupon;
import com.eatpizzaquickly.couponservice.entity.CouponType;
import com.eatpizzaquickly.couponservice.entity.DiscountType;
import com.eatpizzaquickly.couponservice.entity.UserCoupon;
import com.eatpizzaquickly.couponservice.exception.*;
import com.eatpizzaquickly.couponservice.kafka.CouponEvent;
import com.eatpizzaquickly.couponservice.kafka.CouponEventProducer;
import com.eatpizzaquickly.couponservice.repository.CouponsRepository;
import com.eatpizzaquickly.couponservice.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CouponService {

    private static final String COUPON_CACHE = "coupon";
    private static final String USER_COUPONS_CACHE = "userCoupons";
    private static final String LOCK_PREFIX = "coupon:lock:";
    private static final long WAIT_TIME = 3L;
    private static final long LEASE_TIME = 5L;


    private final CouponsRepository couponsRepository;
    private final UserCouponRepository userCouponRepository;
    private final UserClient userClient;
    private final ApplicationEventPublisher eventPublisher;
    private final CouponCacheService couponCacheService;
    private final RedissonClient redissonClient;
    private final CouponEventProducer couponEventProducer;



    @Transactional
    @CacheEvict(value = {COUPON_CACHE, USER_COUPONS_CACHE}, allEntries = true)
    public CouponResponseDto createCoupon(CouponRequestDto couponRequestDto) {
        Coupon coupon = Coupon.builder()
                .couponName(couponRequestDto.getCouponName())
                .couponCode(couponRequestDto.getCouponCode())
                .couponType(couponRequestDto.getCouponType())
                .discountType(couponRequestDto.getDiscountType())
                .discount(couponRequestDto.getDiscount())
                .price(couponRequestDto.getPrice())
                .quantity(couponRequestDto.getQuantity())
                .expiryDate(couponRequestDto.getExpiryDate())
                .build();
        couponsRepository.save(coupon);
        return CouponResponseDto.from(coupon);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = COUPON_CACHE, key = "#couponCode"),
            @CacheEvict(value = USER_COUPONS_CACHE, key = "#userId")
    })
    public void issueCouponToUser(Long userId, String couponCode) {
        String lockKey = LOCK_PREFIX + couponCode;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean isLocked = lock.tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new CouponLockException("쿠폰 발급 처리 중입니다. 잠시 후 다시 시도해주세요.");
            }

            // 기본 검증
            UserResponseDto user = userClient.getUserById(userId).getData();
            log.info("user:{}", user);
            log.info("발급 대상 유저 이메일: {}", user.getEmail());
            if (user == null) {
                throw new UserNotFoundException("가입되지 않은 유저입니다.");
            }
            if (user.getEmail() == null) {
                log.warn("사용자의 이메일이 없습니다. 사용자 ID: {}", userId);
                throw new IllegalArgumentException("이메일이 없는 사용자에게 쿠폰을 발급할 수 없습니다.");
            }

            Coupon coupon = couponCacheService.findCouponByCouponCode(couponCode);
            if (coupon.getCouponType() != CouponType.LIMIT) {
                throw new CouponTypeMissMatched("쿠폰 타입을 다시 확인 해 주세요");
            }

            if (coupon.getQuantity() <= 0) {
                throw new CouponOutOfStockException("쿠폰 수량이 모두 소진되었습니다.");
            }

            boolean alreadyIssued = userCouponRepository.existsByUserIdAndCouponId(userId, coupon.getId());
            if (alreadyIssued) {
                throw new DuplicateCouponException("이미 발급받은 쿠폰입니다.");
            }

            UserCoupon userCoupon = UserCoupon.builder()
                    .userId(userId)
                    .couponId(coupon.getId())
                    .expiryDate(coupon.getExpiryDate())
                    .build();
            userCouponRepository.save(userCoupon);

            coupon.decreaseQuantity();
            couponsRepository.save(coupon);

            // 이벤트 발행 수정
            CouponEvent event = CouponEvent.builder()
                    .eventType("COUPON_ISSUED")
                    .couponId(coupon.getId())
                    .userId(userId)
                    .email(user.getEmail())
                    .couponCode(couponCode)
                    .timestamp(LocalDateTime.now())
                    .notificationMessage(String.format("%s님, [%s] 쿠폰이 발급되었습니다. \n%s까지 사용 가능합니다.",
                            user.getNickname(),
                            coupon.getCouponName(),
                            coupon.getExpiryDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
                    .build();
            couponEventProducer.sendCouponEvent(event);

            eventPublisher.publishEvent(new UserCouponsChangedEvent(this, userId));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CouponLockException("쿠폰 발급 처리 중 인터럽트가 발생했습니다.");
        } catch (Exception e) {
            log.error("쿠폰 발급 중 오류가 발생했습니다. 사용자 ID: {}, 쿠폰 코드: {}. 예외: {}",
                    userId, couponCode, e.getMessage(), e);
            throw e;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }


    @Transactional
    @CacheEvict(value = COUPON_CACHE, key = "#couponId")
    public void issueCouponToAllUsers(Long couponId) {
        String lockKey = LOCK_PREFIX + "bulk:" + couponId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean isLocked = lock.tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new CouponLockException("대량 쿠폰 발급 처리 중입니다. 잠시 후 다시 시도해주세요.");
            }

            Coupon originalCoupon = couponCacheService.findCouponById(couponId);
            if (originalCoupon.getCouponType() != CouponType.ALL) {
                throw new CouponTypeMissMatched("쿠폰 타입을 다시 확인 해 주세요");
            }

            // 모든 사용자 정보를 한 번에 가져옵니다.
            List<UserResponseDto> users = userClient.getAllUsers();

            if (originalCoupon.getQuantity() < users.size()) {
                throw new CouponOutOfStockException("모든 사용자에게 발급할 수량이 부족합니다.");
            }

            // 각 사용자에게 쿠폰을 발급
            List<UserCoupon> userCoupons = users.stream()
                    .map(user -> UserCoupon.builder()
                            .userId(user.getId())
                            .couponId(originalCoupon.getId())
                            .expiryDate(originalCoupon.getExpiryDate())
                            .isUsed(false)
                            .build())
                    .toList();
            userCouponRepository.saveAll(userCoupons);

            // 쿠폰 수량 감소 및 저장
            originalCoupon.decreaseQuantity(users.size());
            couponsRepository.save(originalCoupon);

            // 각 사용자별로 이벤트 발행
            users.forEach(user -> {
                CouponEvent event = CouponEvent.builder()
                        .eventType("BULK_COUPON_ISSUED")
                        .couponId(originalCoupon.getId())
                        .userId(user.getId())
                        .email(user.getEmail())
                        .couponCode(originalCoupon.getCouponCode())
                        .timestamp(LocalDateTime.now())
                        .notificationMessage(String.format("%s님, [%s] 쿠폰이 발급되었습니다. \n%s까지 사용 가능합니다.",
                                user.getNickname(),
                                originalCoupon.getCouponName(),
                                originalCoupon.getExpiryDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
                        .build();
                couponEventProducer.sendCouponEvent(event);
            });

            // 캐시 및 이벤트 갱신
            couponCacheService.clearAllUserCouponsCache();
            users.forEach(user ->
                    eventPublisher.publishEvent(new UserCouponsChangedEvent(this, user.getId())));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CouponLockException("대량 쿠폰 발급 처리 중 인터럽트가 발생했습니다.", e);
        } catch (Exception e) {
            log.error("대량 쿠폰 발급 중 오류가 발생했습니다. 쿠폰 ID: {}. 예외: {}", couponId, e.getMessage(), e);
            throw e;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Cacheable(value = USER_COUPONS_CACHE, key = "#userId", unless = "#result.isEmpty()")
    public List<CouponResponseDto> findAvailableCoupons(Long userId) {
        List<UserCoupon> activeUserCoupons = userCouponRepository.findByUserIdAndIsUsedFalse(userId);
        if (activeUserCoupons.isEmpty()) {
            return Collections.emptyList();
        }

        return activeUserCoupons.stream()
                .map(userCoupon -> {
                    Coupon coupon = couponCacheService.findCouponById(userCoupon.getCouponId());
                    return CouponResponseDto.from(coupon);
                })
                .toList();
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = USER_COUPONS_CACHE, key = "#userCouponId"),
    })
    public Long applyCoupon(Long userCouponId, Long originalPrice) {
        String lockKey = LOCK_PREFIX + "apply:" + userCouponId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean isLocked = lock.tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new CouponLockException("쿠폰 사용 처리 중입니다. 잠시 후 다시 시도해주세요.");
            }

            UserCoupon userCoupon = userCouponRepository.findById(userCouponId)
                    .orElseThrow(() -> new CouponNotFoundException("사용자 쿠폰이 존재하지 않습니다"));

            Coupon coupon = couponCacheService.findCouponById(userCoupon.getCouponId());

            if (!coupon.isCouponActive() || userCoupon.getIsUsed()) {
                throw new CouponActiveException("사용 가능한 쿠폰이 아닙니다.");
            }

            Long discountedPrice = calculateDiscountedPrice(originalPrice, coupon);

            userCoupon.markAsUsed();
            userCouponRepository.save(userCoupon);

            couponCacheService.clearUserCouponsCache(userCoupon.getUserId());
            eventPublisher.publishEvent(new UserCouponsChangedEvent(this, userCoupon.getUserId()));

            return discountedPrice;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CouponLockException("쿠폰 사용 처리 중 인터럽트가 발생했습니다.", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private Long calculateDiscountedPrice(Long originalPrice, Coupon coupon) {
        Long discountedPrice = originalPrice;
        if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            discountedPrice -= (originalPrice * coupon.getDiscount() / 100);
        } else if (coupon.getDiscountType() == DiscountType.AMOUNT) {
            discountedPrice -= coupon.getDiscount();
        }
        return Math.max(discountedPrice, 0);
    }

    // 단일 쿠폰 발급 전 검증
    public Coupon validateSingleCoupon(Long userId, String couponCode) {
        UserResponseDto user = userClient.getUserById(userId).getData();
        if (user == null) {
            throw new UserNotFoundException("가입되지 않은 유저입니다.");
        }

        Coupon coupon = couponCacheService.findCouponByCouponCode(couponCode);
        if (coupon.getCouponType() != CouponType.LIMIT) {
            throw new CouponTypeMissMatched("쿠폰 타입을 다시 확인 해 주세요");
        }

        return coupon;
    }

    // 대량 쿠폰 발급 전 검증
    public Coupon validateBulkCoupon(Long couponId) {
        Coupon coupon = couponCacheService.findCouponById(couponId);
        if (coupon.getCouponType() != CouponType.ALL) {
            throw new CouponTypeMissMatched("쿠폰 타입을 다시 확인 해 주세요");
        }

        return coupon;
    }


}
