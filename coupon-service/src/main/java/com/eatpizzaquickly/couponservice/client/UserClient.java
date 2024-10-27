package com.eatpizzaquickly.couponservice.client;

import com.eatpizzaquickly.couponservice.dto.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/api/v1/users/{userId}")
    UserResponseDto getUserById(@PathVariable("userId") Long userId);

    @GetMapping("/api/v1/users")
    List<Long> getAllUserIds();
}