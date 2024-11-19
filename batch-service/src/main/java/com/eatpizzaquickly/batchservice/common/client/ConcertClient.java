package com.eatpizzaquickly.batchservice.common.client;

import com.eatpizzaquickly.batchservice.settlement.dto.request.HostIdRequestDto;
import com.eatpizzaquickly.batchservice.settlement.dto.response.ConcertHostResponseDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "concert-service")
public interface ConcertClient {
    String CIRCUIT_BREAKER_NAME = "concertService";

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "findHostIdsByConcertIdsFallback")
    @PostMapping("/api/v1/concerts/hosts")
    ResponseEntity<ConcertHostResponseDto> findHostIdsByConcertIds(@RequestBody HostIdRequestDto hostIdRequestDto);

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "resetTopConcertsFallback")
    @PostMapping("/api/v1/concerts/top")
    void resetTopConcerts();

    // Fallback methods
    default void findHostIdsByConcertIdsFallback(Exception ex) {
        // 호스트 ID 조회 실패 시 null 반환
    }

    default void resetTopConcertsFallback(Exception ex) {
        // Top 콘서트 리셋 실패 시 로깅 처리
        // 배치 작업이므로 다음 배치 실행 시 재시도 될 것임
    }
}