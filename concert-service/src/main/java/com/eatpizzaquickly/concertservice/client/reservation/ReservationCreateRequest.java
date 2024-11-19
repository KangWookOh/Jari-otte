package com.eatpizzaquickly.concertservice.client.reservation;

import lombok.Getter;

@Getter
public class ReservationCreateRequest {
    private int price;
    private Long concertId;
    private Long seatId;
    private Long userId;
}
