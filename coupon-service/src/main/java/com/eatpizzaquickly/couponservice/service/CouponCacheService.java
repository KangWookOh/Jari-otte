package com.eatpizzaquickly.couponservice.service;
import com.eatpizzaquickly.couponservice.common.config.UserCouponsChangedEvent;
import com.eatpizzaquickly.couponservice.dto.CouponResponseDto;
import com.eatpizzaquickly.couponservice.entity.Coupon;
import com.eatpizzaquickly.couponservice.entity.UserCoupon;
import com.eatpizzaquickly.couponservice.exception.CouponNotFoundException;
import com.eatpizzaquickly.couponservice.repository.CouponsRepository;
import com.eatpizzaquickly.couponservice.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CouponCacheService {
    private final CouponsRepository couponsRepository;
    private final UserCouponRepository userCouponRepository;
    private static final String COUPON_CACHE = "coupon";
    private static final String USER_COUPONS_CACHE = "userCoupons";
    private final CacheManager cacheManager;


    @Cacheable(value = "coupon", key = "#couponCode")
    public Coupon findCouponByCouponCode(String couponCode) {
        return couponsRepository.findByCouponCode(couponCode)
                .orElseThrow(() -> new CouponNotFoundException("쿠폰이 존재하지 않습니다"));
    }

    @Cacheable(value = "coupon", key = "#couponId")
    public Coupon findCouponById(Long couponId) {
        return couponsRepository.findById(couponId)
                .orElseThrow(() -> new CouponNotFoundException("쿠폰이 존재하지 않습니다"));
    }


    public void clearUserCouponsCache(Long userId) {
        Cache userCouponsCache = cacheManager.getCache(USER_COUPONS_CACHE);
        if (userCouponsCache != null) {
            userCouponsCache.evict(userId);
        }
    }

    public void clearAllUserCouponsCache() {
        Cache userCouponsCache = cacheManager.getCache(USER_COUPONS_CACHE);
        if (userCouponsCache != null) {
            userCouponsCache.clear();
        }
    }
}
