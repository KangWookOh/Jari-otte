package com.eatpizzaquickly.batchservice.common.client;

import com.eatpizzaquickly.batchservice.coupon.dto.CouponDto;
import com.eatpizzaquickly.batchservice.coupon.dto.UserCouponDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@FeignClient(name = "coupon-service")
public interface CouponServiceClient {

    String CIRCUIT_BREAKER_NAME = "couponService";

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getExpiredCouponsFallback")
    @GetMapping("/api/v1/coupons/expired")
    List<CouponDto> getExpiredCoupons(@RequestParam("currentDate") String currentDate,
                                      @RequestParam("page") int page,
                                      @RequestParam("size") int size);

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getExpiredUserCouponsAfterFallback")
    @GetMapping("/api/v1/user-coupons/expired/after")
    Page<UserCouponDto> getExpiredUserCouponsAfter(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String currentDate,
            @RequestParam(required = false) Long lastSeenId,
            @RequestParam int size
    );

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "deactivateExpiredCouponsFallback")
    @PutMapping("/api/v1/coupons/deactivate")
    void deactivateExpiredCoupons(@RequestBody List<Long> couponIds);

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getExpiredUserCouponsFallback")
    @GetMapping("/api/v1/user-coupons/expired")
    Page<UserCouponDto> getExpiredUserCoupons(@RequestParam("currentDate") String currentDate,
                                              @RequestParam("page") int page,
                                              @RequestParam("size") int size);

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "deleteExpiredUserCouponsFallback")
    @DeleteMapping("/api/v1/user-coupons")
    void deleteExpiredUserCoupons(@RequestBody List<Long> userCouponIds);

    // Fallback methods
    default List<CouponDto> getExpiredCouponsFallback(String currentDate, int page, int size, Exception ex) {
        return new ArrayList<>();
    }

    default Page<UserCouponDto> getExpiredUserCouponsAfterFallback(String currentDate, Long lastSeenId, int size, Exception ex) {
        return new PageImpl<>(new ArrayList<>());
    }

    default void deactivateExpiredCouponsFallback(List<Long> couponIds, Exception ex) {
        // Log the failure or handle it according to your business requirements
    }

    default Page<UserCouponDto> getExpiredUserCouponsFallback(String currentDate, int page, int size, Exception ex) {
        return new PageImpl<>(new ArrayList<>());
    }

    default void deleteExpiredUserCouponsFallback(List<Long> userCouponIds, Exception ex) {
        // Log the failure or handle it according to your business requirements
    }
}