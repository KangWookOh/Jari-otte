package com.eatpizzaquickly.couponservice.client;

import com.eatpizzaquickly.couponservice.common.advice.ApiResponse;
import com.eatpizzaquickly.couponservice.dto.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

// UserServiceClient.java
@FeignClient(name = "user-service", url = "${gateway.url}")
public interface UserServiceClient {

    @GetMapping("/api/users/check")
    Boolean checkUserExists(@RequestParam("id") Long id);
}
