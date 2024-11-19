package com.eatpizzaquickly.batchservice.common.client;

import com.eatpizzaquickly.batchservice.common.config.FeignConfig;
import com.eatpizzaquickly.batchservice.settlement.dto.request.HostPointRequestDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "user-service", configuration = FeignConfig.class)
public interface UserClient {

    String CIRCUIT_BREAKER_NAME = "userService";

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "addPointsToHostFallback")
    @PostMapping("/api/v1/users/addpoints")
    ResponseEntity<String> addPointsToHost(@RequestBody List<HostPointRequestDto> hostpoints);

    // Fallback method
    default ResponseEntity<String> addPointsToHostFallback(List<HostPointRequestDto> hostpoints, Exception ex) {
        return ResponseEntity.internalServerError()
                .body("Failed to add points to hosts. Will retry in next batch.");
    }
}