package com.eatpizzaquickly.batchservice.common.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "concert-service")
public interface ConcertClient {

    String CIRCUIT_BREAKER_NAME = "concertService";

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "findHostIdByConcertIdFallback")
    @GetMapping("/api/v1/concerts/{concertId}/host")
    Long findHostIdByConcertId(@PathVariable(name = "concertId") Long concertId);

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "resetTopConcertsFallback")
    @PostMapping("/api/v1/concerts/top")
    void resetTopConcerts();

    // Fallback methods
    default Long findHostIdByConcertIdFallback(Long concertId, Exception ex) {
        // 호스트 ID 조회 실패 시 null 반환
        return null;
    }

    default void resetTopConcertsFallback(Exception ex) {
        // Top 콘서트 리셋 실패 시 로깅 처리
        // 배치 작업이므로 다음 배치 실행 시 재시도 될 것임
    }
}