package com.eatpizzaquickly.reservationservice.reservation.client.feign;

import com.eatpizzaquickly.reservationservice.common.advice.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "concert-service")
public interface ConcertClient {

    @PostMapping("/api/v1/concerts/{concertId}/seats/{seatId}/restore")
    ResponseEntity<ApiResponse<Void>> restoreSeat(@PathVariable("concertId") Long concertId, @PathVariable Long seatId);
}
