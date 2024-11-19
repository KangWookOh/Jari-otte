package com.eatpizzaquickly.concertservice.dto;

import com.eatpizzaquickly.concertservice.dto.request.SeatReservationRequest;
import com.eatpizzaquickly.concertservice.entity.Seat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SeatDto {
    private Long id;
    private Integer seatNumber;
    private Boolean isReserved;

    public static SeatDto from(Seat seat) {
        return new SeatDto(seat.getId(), seat.getSeatNumber(), seat.isReserved());
    }

    public static SeatDto from(SeatReservationRequest request) {
        return new SeatDto(request.getSeatId(), request.getSeatNumber(), request.getIsReserved());
    }

}
