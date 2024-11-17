package com.eatpizzaquickly.reservationservice.reservation.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class SeatReservationEvent {
    private Long concertId;
    private Long userId;
    private Long seatId;
    private Integer seatNumber;
    private Integer price;

    @Builder
    private SeatReservationEvent(Long concertId, Long userId, Long seatId, Integer seatNumber, Integer price) {
        this.concertId = concertId;
        this.userId = userId;
        this.seatId = seatId;
        this.seatNumber = seatNumber;
        this.price = price;
    }
}
