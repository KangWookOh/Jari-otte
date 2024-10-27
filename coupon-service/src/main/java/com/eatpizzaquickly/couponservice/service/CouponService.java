package com.eatpizzaquickly.couponservice.service;

import com.eatpizzaquickly.couponservice.client.UserClient;
import com.eatpizzaquickly.couponservice.dto.CouponRequestDto;
import com.eatpizzaquickly.couponservice.dto.CouponResponseDto;
import com.eatpizzaquickly.couponservice.dto.UserResponseDto;
import com.eatpizzaquickly.couponservice.entity.Coupon;
import com.eatpizzaquickly.couponservice.entity.DiscountType;
import com.eatpizzaquickly.couponservice.exception.*;
import com.eatpizzaquickly.couponservice.repository.CouponsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly=true)
@RequiredArgsConstructor
public class CouponService {

    private final CouponsRepository couponsRepository;
    private final UserClient userClient;  // Feign Client 주입


    //어드민이 쿠폰을 생성하는관리하는 로직
    @Transactional
    public CouponResponseDto createCoupon(CouponRequestDto couponRequestDto) {
        Coupon coupon = Coupon.builder()
                .couponName(couponRequestDto.getCouponName())
                .couponCode(couponRequestDto.getCouponCode())
                .couponType(couponRequestDto.getCouponType())
                .discountType(couponRequestDto.getDiscountType())
                .discount(couponRequestDto.getDiscount())
                .price(couponRequestDto.getPrice())
                .quantity(couponRequestDto.getQuantity())
                .build();
        couponsRepository.save(coupon);
        return CouponResponseDto.from(coupon);
    }

    @Transactional
    public void issueCouponToUser(Long userId, String couponCode) {
        // 1. 쿠폰 조회
        Coupon coupon = couponsRepository.findByCouponCode(couponCode)
                .orElseThrow(() -> new CouponNotFoundException("쿠폰이 존재 하지 않습니다"));

        // 2. 쿠폰 수량 체크
        if (coupon.getQuantity() <= 0) {
            throw new CouponOutOfStockException("쿠폰 수량이 모두 소진되었습니다.");
        }

        // 3. 중복 발급 체크
        if (coupon.getUserId() != null && coupon.getUserId().equals(userId)) {
            throw new DuplicateCouponException("이미 발급받은 쿠폰입니다.");
        }

        // 4. 유저 확인 (Feign Client 사용)
        UserResponseDto user = userClient.getUserById(userId);
        if (user == null) {
            throw new UserNotFoundException("가입되지 않는 유저입니다.");
        }

        // 5. 쿠폰 수량 감소 및 사용자 ID 설정
        coupon.decreaseQuantity();
        coupon.updateUserId(userId);  // 사용자를 설정하여 중복 발급 방지
        couponsRepository.save(coupon);
    }

    @Transactional
    public void issueCouponToAllUsers(Long couponId) {
        // 1. 발급할 쿠폰 조회
        Coupon originalCoupon = couponsRepository.findById(couponId)
                .orElseThrow(() -> new CouponNotFoundException("쿠폰이 존재하지 않습니다"));

        // 2. user-service에서 모든 사용자 ID 조회
        List<Long> allUserIds = userClient.getAllUserIds();

        // 3. 남은 수량 확인 후 일괄 발급 처리
        if (originalCoupon.getQuantity() < allUserIds.size()) {
            throw new CouponOutOfStockException("모든 사용자에게 발급할 수량이 부족합니다.");
        }

        // 4. 각 사용자에게 쿠폰 발급
        for (Long userId : allUserIds) {
            // 각 사용자별로 쿠폰 복제 및 userId 설정
            Coupon userCoupon = Coupon.builder()
                    .couponCode(originalCoupon.getCouponCode())
                    .couponName(originalCoupon.getCouponName())
                    .couponType(originalCoupon.getCouponType())
                    .discountType(originalCoupon.getDiscountType())
                    .discount(originalCoupon.getDiscount())
                    .price(originalCoupon.getPrice())
                    .quantity(1)  // 각 사용자는 1개의 쿠폰을 받음
                    .build();
            userCoupon.updateUserId(userId);
            couponsRepository.save(userCoupon);  // 사용자별 쿠폰 저장
        }

        // 5. 원본 쿠폰의 수량 감소 및 저장
        originalCoupon.decreaseQuantity(allUserIds.size());
        couponsRepository.save(originalCoupon);
    }

    public List<CouponResponseDto> findAvailableCoupons(Long userId) {
        // 1. userId에 해당하는 쿠폰 중 활성 상태인 쿠폰만 조회
        List<Coupon> activeCoupons = couponsRepository.findByUserIdAndIsActiveTrue(userId);

        // 2. 쿠폰을 CouponResponseDto로 변환하여 반환
        return activeCoupons.stream()
                .map(CouponResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public Long applyCoupon(Long couponId, Long originalPrice){
        Coupon coupon = couponsRepository.findById(couponId)
                .orElseThrow(()->new CouponNotFoundException("쿠폰이 존재 하지 않습니다."));
        if(!coupon.getIsActive()){
            throw new CouponActiveException("사용가능한 쿠폰이 아닙니다.");
        }
        Long discountedPrice = originalPrice;
        if(coupon.getDiscountType() == DiscountType.PERCENTAGE){
            discountedPrice -=(originalPrice * coupon.getDiscount() / 100);
        }
        else if(coupon.getDiscountType() == DiscountType.AMOUNT){
            discountedPrice -= coupon.getDiscount();
        }
        return  Math.max(discountedPrice, 0);
    }
}
