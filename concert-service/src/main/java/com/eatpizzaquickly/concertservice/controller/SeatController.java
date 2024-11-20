package com.eatpizzaquickly.concertservice.controller;

import com.eatpizzaquickly.concertservice.dto.request.SeatReservationRequest;
import com.eatpizzaquickly.concertservice.dto.response.ApiResponse;
import com.eatpizzaquickly.concertservice.dto.response.AvailableSeatCountResponse;
import com.eatpizzaquickly.concertservice.dto.response.SeatListResponse;
import com.eatpizzaquickly.concertservice.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/v1/concerts")
@RestController
public class SeatController {

    private final SeatService seatService;

    @GetMapping("/{concertId}/seats")
    public ResponseEntity<ApiResponse<SeatListResponse>> getSeatList(@PathVariable Long concertId,
                                                                     @RequestHeader("X-Authenticated-User") Long userId) {
        SeatListResponse seatListResponse = seatService.findSeatList(concertId, userId);
        return ResponseEntity.ok(ApiResponse.success("좌석 조회 성공", seatListResponse));
    }

    @PostMapping("/{concertId}/seats")
    public ResponseEntity<ApiResponse<Void>> reserveSeat(@PathVariable Long concertId,
                                                         @RequestHeader("X-Authenticated-User") Long userId,
                                                         @RequestBody SeatReservationRequest seatReservationRequest) {
        seatService.reserveSeat(concertId, userId, seatReservationRequest);
        return ResponseEntity.ok(ApiResponse.success("좌석 예매 성공"));
    }

    @GetMapping("/{concertId}/seats/count")
    public ResponseEntity<ApiResponse<AvailableSeatCountResponse>> getAvailableSeatCount(@PathVariable Long concertId) {
        AvailableSeatCountResponse availableSeatCount = seatService.getAvailableSeatCount(concertId);
        return ResponseEntity.ok(ApiResponse.success("잔여 좌석 수 조회 성공", availableSeatCount));
    }

    @PostMapping("/{concertId}/seats/{seatId}/restore")
    public ResponseEntity<ApiResponse<Void>> restoreSeat(@PathVariable Long concertId, @PathVariable Long seatId) {
        seatService.restoreSeat(concertId, seatId);
        return ResponseEntity.ok(ApiResponse.success("좌석 복구 성공"));
    }
}
