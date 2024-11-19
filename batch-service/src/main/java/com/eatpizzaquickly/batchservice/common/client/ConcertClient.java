package com.eatpizzaquickly.batchservice.common.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "concert-service")
public interface ConcertClient {

    String CIRCUIT_BREAKER_NAME = "concertService";

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "findHostIdByConcertIdFallback")
    @GetMapping("/api/v1/concerts/{concertId}/host")
    Long findHostIdByConcertId(@PathVariable(name = "concertId") Long concertId);

    // Fallback method
    default Long findHostIdByConcertIdFallback(Long concertId, Exception ex) {
        return null;
    }
}