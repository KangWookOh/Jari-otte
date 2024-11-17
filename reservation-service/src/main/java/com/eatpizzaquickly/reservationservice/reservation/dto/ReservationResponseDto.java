package com.eatpizzaquickly.reservationservice.reservation.dto;

import com.eatpizzaquickly.reservationservice.reservation.entity.Reservation;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
public class ReservationResponseDto {
    private Long concertId;
    private LocalDateTime createdAt;
    private int price;
    private Long seatId;

    public static ReservationResponseDto from(Reservation reservation) {
        return new ReservationResponseDto(
                reservation.getConcertId(),
                reservation.getCreatedAt(),
                reservation.getPrice(),
                reservation.getSeatId()
        );
    }
}
