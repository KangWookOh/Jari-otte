package com.eatpizzaquickly.reservationservice.payment.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "coupon-service")
public interface CouponFeignClient {

    String CIRCUIT_BREAKER_NAME = "couponService";

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "applyCouponFallback")
    @GetMapping("/api/v1/coupons/apply")
    Long applyCoupon(@RequestParam("couponId") Long couponId, @RequestParam("amount") Long amount);

    // Fallback method
    default Long applyCouponFallback(Long couponId, Long amount, Exception ex) {
        // 쿠폰 적용 실패 시 원래 금액을 그대로 반환
        // 할인이 적용되지 않은 원래 금액을 반환함으로써 서비스 연속성 보장
        return amount;
    }
}