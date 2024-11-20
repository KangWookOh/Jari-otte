package com.eatpizzaquickly.reservationservice.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SeatReservationRequest {
    private int price;
    private Long seatId;
    private Integer seatNumber;
    private Boolean isReserved;
}