package com.eatpizzaquickly.concertservice.client.reservation;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "reservation-service")
public interface ReservationServiceClient {

    String CIRCUIT_BREAKER_NAME = "reservationService";

    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "createReservationFallback")
    @PostMapping(value = "/api/v1/reservations", consumes = MediaType.APPLICATION_JSON_VALUE)
    PostReservationResponse createReservation(@RequestBody PostReservationRequest request);

    // Fallback method
    default PostReservationResponse createReservationFallback(PostReservationRequest request, Exception ex) {
        throw new RuntimeException("Failed to create reservation: " + ex.getMessage());
    }
}