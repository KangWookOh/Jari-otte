package com.eatpizzaquickly.couponservice.service;

import com.eatpizzaquickly.couponservice.client.UserClient;
import com.eatpizzaquickly.couponservice.common.config.UserCouponsChangedEvent;
import com.eatpizzaquickly.couponservice.dto.CouponRequestDto;
import com.eatpizzaquickly.couponservice.dto.CouponResponseDto;
import com.eatpizzaquickly.couponservice.dto.UserResponseDto;
import com.eatpizzaquickly.couponservice.entity.Coupon;
import com.eatpizzaquickly.couponservice.enums.CouponType;
import com.eatpizzaquickly.couponservice.enums.DiscountType;
import com.eatpizzaquickly.couponservice.entity.UserCoupon;
import com.eatpizzaquickly.couponservice.exception.*;
import com.eatpizzaquickly.couponservice.kafka.CouponEvent;
import com.eatpizzaquickly.couponservice.kafka.CouponEventProducer;
import com.eatpizzaquickly.couponservice.kafka.SendCouponEmailProducer;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
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
    private final SendCouponEmailProducer sendCouponEmailProducer;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
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
        Coupon savedCoupon = couponsRepository.save(coupon);
        couponCacheService.saveCouponToCache(savedCoupon);
        redisTemplate.opsForValue().set("coupon:" + couponRequestDto.getCouponCode() + ":quantity", String.valueOf(couponRequestDto.getQuantity()));
        LocalDateTime expiryDate = coupon.getExpiryDate().atStartOfDay();
        long daysUntilExpiry = ChronoUnit.DAYS.between(LocalDateTime.now(), expiryDate);
        redisTemplate.expire("coupon:" + savedCoupon.getCouponCode() + ":users", Duration.ofDays(daysUntilExpiry));


        return CouponResponseDto.from(savedCoupon);
    }


    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Caching(evict = {
            @CacheEvict(value = COUPON_CACHE, key = "#couponCode"),
            @CacheEvict(value = USER_COUPONS_CACHE, key = "#userId")
    })
    public void issueCouponToUser(Long userId, String couponCode) {
        final String ISSUE_COUPON_SCRIPT = """
            local couponKey = KEYS[1]
            local userSetKey = KEYS[2]
            local userId = ARGV[1]
            
            -- 이미 발급된 쿠폰인지 확인
            if redis.call('sismember', userSetKey, userId) == 1 then
                return -1  -- 이미 발급됨
            end
            
            -- 쿠폰 수량 확인 및 감소
            local currentQuantity = redis.call('get', couponKey)
            if not currentQuantity or tonumber(currentQuantity) <= 0 then
                return 0  -- 수량 부족
            end
            
            redis.call('decr', couponKey)
            redis.call('sadd', userSetKey, userId)
            return 1  -- 발급 성공
            """;

        UserResponseDto user = userClient.getUserById(userId).getData();
        if (user == null || user.getEmail() == null) {
            throw new UserNotFoundException("유효하지 않은 사용자입니다.");
        }

        Coupon coupon = couponCacheService.findCouponByCouponCode(couponCode);
        if (coupon.getCouponType() != CouponType.LIMIT) {
            throw new CouponTypeMissMatched("쿠폰 타입을 다시 확인 해 주세요");
        }

        String couponQuantityKey = "coupon:" + couponCode + ":quantity";
        String couponUserSetKey = "coupon:" + couponCode + ":users";

        Long result = redisTemplate.execute(
                RedisScript.of(ISSUE_COUPON_SCRIPT, Long.class),
                List.of(couponQuantityKey, couponUserSetKey),
                userId.toString()
        );

        if (result == null || result == 0) {
            throw new CouponTypeMissMatched("쿠폰 수량을 다시 확인 해 주세요");
        } else if (result == -1) {
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

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCommit() {
                        try {
                            CouponEvent event = CouponEvent.builder()
                                    .eventType("SINGLE_ISSUE")
                                    .couponId(coupon.getId())
                                    .userId(userId)
                                    .email(user.getEmail())
                                    .couponCode(couponCode)
                                    .timestamp(LocalDateTime.now())
                                    .notificationMessage(String.format("%s님, [%s] 쿠폰이 발급되었습니다. %s까지 사용 가능합니다.",
                                            user.getNickname(), coupon.getCouponName(), coupon.getExpiryDate()))
                                    .build();
                            couponEventProducer.sendCouponEvent(event);
                            sendCouponEmailProducer.sendCouponEmail(event);
                            eventPublisher.publishEvent(new UserCouponsChangedEvent(this, userId));
                        } catch (Exception e) {
                            log.error("쿠폰 이벤트 발행 실패: ", e);
                        }
                    }
                });
    }



    @Transactional
    @CacheEvict(value = COUPON_CACHE, key = "#couponId")
    public void issueCouponToAllUsers(Long couponId) {
        final String BULK_ISSUE_COUPON_SCRIPT = """
            local couponKey = KEYS[1]
            local userSetKey = KEYS[2]
            local userCount = tonumber(ARGV[1])
            
            -- 쿠폰 수량 확인
            local currentQuantity = redis.call('get', couponKey)
            if not currentQuantity or tonumber(currentQuantity) < userCount then
                return 0  -- 수량 부족
            end
            
            -- 수량 감소
            redis.call('decrby', couponKey, userCount)
            return 1  -- 발급 성공
            """;

        Coupon originalCoupon = couponCacheService.findCouponById(couponId);
        if (originalCoupon.getCouponType() != CouponType.ALL) {
            throw new CouponTypeMissMatched("쿠폰 타입을 다시 확인 해 주세요");
        }

        List<UserResponseDto> users = userClient.getAllUsers();

        // 중복 발급 체크 후 필터링
        List<UserCoupon> userCoupons = users.stream()
                .filter(user -> !isCouponAlreadyIssued(user.getId(), couponId))
                .map(user -> UserCoupon.builder()
                        .userId(user.getId())
                        .couponId(originalCoupon.getId())
                        .expiryDate(originalCoupon.getExpiryDate())
                        .isUsed(false)
                        .build())
                .toList();

        int issuableCount = userCoupons.size();
        if (issuableCount == 0) {
            throw new DuplicateCouponException("모든 사용자가 이미 쿠폰을 보유하고 있습니다.");
        }

        String couponQuantityKey = "coupon:" + originalCoupon.getCouponCode() + ":quantity";
        String couponUserSetKey = "coupon:" + originalCoupon.getCouponCode() + ":users";

        Long result = redisTemplate.execute(
                RedisScript.of(BULK_ISSUE_COUPON_SCRIPT, Long.class),
                List.of(couponQuantityKey, couponUserSetKey),
                String.valueOf(issuableCount)
        );

        if (result == null || result == 0) {
            throw new CouponOutOfStockException("모든 사용자에게 발급할 수량이 부족합니다.");
        }

        userCouponRepository.saveAll(userCoupons);
        originalCoupon.decreaseQuantity(issuableCount);
        couponsRepository.save(originalCoupon);

        userCoupons.forEach(userCoupon -> {
            UserResponseDto user = users.stream()
                    .filter(u -> u.getId().equals(userCoupon.getUserId()))
                    .findFirst()
                    .orElseThrow();

            CouponEvent event = CouponEvent.builder()
                    .eventType("BULK_ISSUE")
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
            sendCouponEmailProducer.sendCouponEmail(event);
        });

        couponCacheService.clearAllUserCouponsCache();
        userCoupons.forEach(userCoupon ->
                eventPublisher.publishEvent(new UserCouponsChangedEvent(this, userCoupon.getUserId())));
    }


    @Cacheable(value = USER_COUPONS_CACHE, key = "#userId", unless = "#result.isEmpty()")
    public List<CouponResponseDto> findAvailableCoupons(Long userId) {
        List<UserCoupon> activeUserCoupons = userCouponRepository.findByUserIdAndIsUsedFalse(userId);
        if (activeUserCoupons.isEmpty()) {
            return Collections.emptyList();
        }

        return activeUserCoupons.stream()
                .map(userCoupon -> couponCacheService.findCouponById(userCoupon.getCouponId()))
                .filter(coupon -> Boolean.TRUE.equals(coupon.getIsActive())) // 활성 상태 필터링
                .map(CouponResponseDto::from)
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

    // 중복 발급 방지 메서드
    public boolean isCouponAlreadyIssued(Long userId, Long couponId) {
        return userCouponRepository.existsByUserIdAndCouponId(userId, couponId);
    }
}
