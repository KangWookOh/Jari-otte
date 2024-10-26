package com.eatpizzaquickly.reservationservice.reservation.dto;

import com.eatpizzaquickly.reservationservice.reservation.entity.Reservation;
import com.eatpizzaquickly.reservationservice.reservation.entity.ReservationStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostReservationResponse {
    private final Long id;
    private final Long seatId;
    private final int price;
    private final ReservationStatus status;
    private final LocalDateTime created_at;
    private final Long userId;

    public PostReservationResponse(Reservation reservation) {
        this.id = reservation.getId();
        this.seatId = reservation.getSeatId();
        this.price = reservation.getPrice();
        this.status = reservation.getStatus();
        this.created_at = reservation.getCreatedAt();
        this.userId = reservation.getUserId();
    }

    public static PostReservationResponse from(Reservation reservation) {
        return new PostReservationResponse(
                reservation
        );
    }
}