package com.eatpizzaquickly.reservationservice.payment.client;

import com.eatpizzaquickly.reservationservice.payment.dto.response.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("/api/v1/users/{userId}")
    ApiResponse<UserResponseDto> getUserById(@PathVariable("userId") Long userId);

}