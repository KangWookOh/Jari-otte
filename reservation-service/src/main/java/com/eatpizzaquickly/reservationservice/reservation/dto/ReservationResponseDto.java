package com.eatpizzaquickly.reservationservice.reservation.dto;

import com.eatpizzaquickly.reservationservice.reservation.entity.Reservation;
import com.eatpizzaquickly.reservationservice.common.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class ReservationResponseDto {
    private Long concertId;
    private LocalDateTime createdAt;
    private int price;
    private Long seatId;
    private Integer seatNumber;
    private ReservationStatus reservationStatus;

    public static ReservationResponseDto from(Reservation reservation) {
        return new ReservationResponseDto(
                reservation.getConcertId(),
                reservation.getCreatedAt(),
                reservation.getPrice(),
                reservation.getSeatId(),
                reservation.getSeatNumber(),
                reservation.getStatus()
        );
    }
}
