package com.eatpizzaquickly.batchservice.coupon.client;

import com.eatpizzaquickly.batchservice.coupon.dto.CouponDto;
import com.eatpizzaquickly.batchservice.coupon.dto.UserCouponDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "coupon-service")
public interface CouponServiceClient {
    @GetMapping("/api/v1/coupons/expired")
    List<CouponDto> getExpiredCoupons(@RequestParam("currentDate") String currentDate,
                                      @RequestParam("page") int page,
                                      @RequestParam("size") int size);

    @GetMapping("/api/v1/user-coupons/expired/after")
    Page<UserCouponDto> getExpiredUserCouponsAfter(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String currentDate,
            @RequestParam(required = false) Long lastSeenId,
            @RequestParam int size
    );

    @PutMapping("/api/v1/coupons/deactivate")
    void deactivateExpiredCoupons(@RequestBody List<Long> couponIds);

    @GetMapping("/api/v1/user-coupons/expired")
    Page<UserCouponDto> getExpiredUserCoupons(@RequestParam("currentDate") String currentDate,
                                                   @RequestParam("page") int page,
                                                   @RequestParam("size") int size);

    @DeleteMapping("/api/v1/user-coupons")
    void deleteExpiredUserCoupons(@RequestBody List<Long> userCouponIds);
}