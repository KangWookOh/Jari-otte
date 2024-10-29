package com.eatpizzaquickly.couponservice.kafka;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class CouponEvent {
    private String eventType;
    private Long couponId;
    private String couponCode;
    private Long userId;
    private LocalDateTime timestamp;

    @Builder
    public CouponEvent(String eventType, Long couponId, Long userId,String couponCode,LocalDateTime timestamp) {
        this.eventType = eventType;
        this.couponId = couponId;
        this.userId = userId;
        this.couponCode = couponCode;
        this.timestamp = timestamp;
    }
}
