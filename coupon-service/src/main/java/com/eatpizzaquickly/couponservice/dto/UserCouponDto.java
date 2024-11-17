package com.eatpizzaquickly.couponservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class UserCouponDto {

    private Long id;

    private Long userId;

    private Long couponId;

    private LocalDate expiryDate;

    @Builder
    public UserCouponDto(Long id, Long userId, Long couponId, LocalDate expiryDate) {
        this.id = id;
        this.userId = userId;
        this.couponId = couponId;
        this.expiryDate = expiryDate;
    }
}
