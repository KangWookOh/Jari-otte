package com.eatpizzaquickly.couponservice.repository;


import com.eatpizzaquickly.couponservice.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponsRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCouponCode(String couponCode);

    Optional<Coupon> findByUserId(Long userId);

    boolean existsByCouponIdAndUserId(Long couponId, Long userId);


}
