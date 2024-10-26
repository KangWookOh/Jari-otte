package com.eatpizzaquickly.reservationservice.reservation.service;

import com.eatpizzaquickly.reservationservice.common.exception.NotFoundException;
import com.eatpizzaquickly.reservationservice.common.exception.UnauthorizedException;
import com.eatpizzaquickly.reservationservice.reservation.dto.PostReservationRequest;
import com.eatpizzaquickly.reservationservice.reservation.dto.PostReservationResponse;
import com.eatpizzaquickly.reservationservice.reservation.entity.Reservation;
import com.eatpizzaquickly.reservationservice.reservation.entity.ReservationStatus;
import com.eatpizzaquickly.reservationservice.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;

    @Transactional
    public PostReservationResponse createReservation(PostReservationRequest request) {

        // 엔티티 변환
        Reservation reservation = Reservation.builder()
                .price(request.getPrice())
                .userId(request.getUserId())
                .seatId(request.getSeatId())
                .reservationStatus(ReservationStatus.PENDING)
                .concertId(request.getConcertId())
                .build();

        // 저장 후 반환
        Reservation savedReservation = reservationRepository.save(reservation);
        return PostReservationResponse.from(savedReservation);
    }

    @Transactional
    public void cancelReservation(Long userId, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(
                () -> new NotFoundException("예약이 존재하지 않습니다."));

        if (!userId.equals(reservation.getUserId())) {
            throw new UnauthorizedException("예약한 유저가 아닙니다.");
        }

        reservation.setStatus(ReservationStatus.CANCELED);

        reservationRepository.save(reservation);
    }
}
