package com.eatpizzaquickly.couponservice.repository;

import com.eatpizzaquickly.couponservice.entity.UserCoupon;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {
    boolean existsByUserIdAndCouponId(Long userId, Long couponId);

    List<UserCoupon> findByUserIdAndIsUsedFalse(Long userId);




}
