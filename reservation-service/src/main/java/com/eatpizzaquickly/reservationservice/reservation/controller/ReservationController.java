package com.eatpizzaquickly.reservationservice.reservation.controller;

import com.eatpizzaquickly.reservationservice.common.advice.ApiResponse;
import com.eatpizzaquickly.reservationservice.reservation.dto.ReservationResponseDto;
import com.eatpizzaquickly.reservationservice.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationService reservationService;

    //    @PostMapping
//    public PostReservationResponse createReservation(
//            @RequestBody PostReservationRequest request
//    ) {
//        PostReservationResponse response = reservationService.createReservation(request);
//        return response;
//    }
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReservationResponseDto>>> getReservations(
            @RequestHeader("X-Authenticated-User") Long userId,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "4") int size
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "예매 내역 조회 성공",
                        reservationService.getReservations(userId, page, size)
                ));
    }

    @PostMapping("/{reservationId}/cancel")
    public ResponseEntity<ApiResponse> cancelReservation(
            @RequestHeader("X-Authenticated-User") Long userId,
            @PathVariable Long reservationId
    ) {
        reservationService.cancelReservation(userId, reservationId);
        return ResponseEntity.ok(ApiResponse.success("예약 취소", reservationId));
    }
}
