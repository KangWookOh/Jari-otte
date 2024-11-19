package com.eatpizzaquickly.reservationservice.payment.client;

import com.eatpizzaquickly.reservationservice.common.config.FeignConfig;
import com.eatpizzaquickly.reservationservice.payment.dto.request.HostPointRequestDto;
import com.eatpizzaquickly.reservationservice.payment.dto.response.UserResponseDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "user-service", configuration = FeignConfig.class)
public interface UserClient {

    String CIRCUIT_BREAKER_NAME = "userService";

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "getUserByIdFallback")
    @GetMapping("/api/v1/users/{userId}")
    ApiResponse<UserResponseDto> getUserById(@PathVariable("userId") Long userId);

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "addPointsToHostFallback")
    @PostMapping("/api/v1/users/addpoints")
    ResponseEntity<String> addPointsToHost(@RequestBody List<HostPointRequestDto> hostpoints);

    // Fallback method
    default ApiResponse<UserResponseDto> getUserByIdFallback(Long userId, Exception ex) {
        // 사용자 정보 조회 실패 시
        return ApiResponse.success("Failed to fetch user information", null);
    }

    default ResponseEntity<String> addPointsToHostFallback(List<HostPointRequestDto> hostpoints, Exception ex) {
        // 포인트 적립 실패 시
        // 실패한 요청을 로깅하거나 재시도 큐에 넣는 등의 처리를 할 수 있습니다.
        return ResponseEntity.internalServerError()
                .body("Failed to add points to hosts. Will retry later.");
    }
}