package com.eatpizzaquickly.couponservice.client;

import com.eatpizzaquickly.couponservice.dto.UserResponseDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;
import java.util.ArrayList;

@FeignClient(name = "user-service")
public interface UserClient {

    String CIRCUIT_BREAKER_NAME = "userService";

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getUserByIdFallback")
    @GetMapping("/api/v1/users/{userId}")
    ApiResponse<UserResponseDto> getUserById(@PathVariable("userId") Long userId);

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getAllUsersFallback")
    @GetMapping("/api/v1/users")
    List<UserResponseDto> getAllUsers();

    // Fallback methods
    default ApiResponse<UserResponseDto> getUserByIdFallback(Long userId, Exception ex) {
        return new ApiResponse<>("Service temporarily unavailable", null);
    }

    default List<UserResponseDto> getAllUsersFallback(Exception ex) {
        return new ArrayList<>();
    }
}