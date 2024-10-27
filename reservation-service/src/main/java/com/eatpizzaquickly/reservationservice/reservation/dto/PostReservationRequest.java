package com.eatpizzaquickly.reservationservice.reservation.dto;

import lombok.Getter;

@Getter
public class PostReservationRequest {
    private int price;
    private Long concertId;
    private Long seatId;
    private Long userId;
}
