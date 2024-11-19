package com.eatpizzaquickly.reservationservice.reservation.service;

import com.eatpizzaquickly.reservationservice.common.exception.NotFoundException;
import com.eatpizzaquickly.reservationservice.common.exception.UnauthorizedException;
import com.eatpizzaquickly.reservationservice.reservation.dto.PostReservationResponse;
import com.eatpizzaquickly.reservationservice.reservation.dto.ReservationResponseDto;
import com.eatpizzaquickly.reservationservice.reservation.dto.ReservationCreateRequest;
import com.eatpizzaquickly.reservationservice.reservation.entity.Reservation;
import com.eatpizzaquickly.reservationservice.common.enums.ReservationStatus;
import com.eatpizzaquickly.reservationservice.reservation.exception.ReservationCreationException;
import com.eatpizzaquickly.reservationservice.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;

    @Transactional
    public PostReservationResponse createReservation(ReservationCreateRequest request) {
        try {
            // 엔티티 변환
            Reservation reservation = Reservation.builder()
                    .price(request.getPrice())
                    .userId(request.getUserId())
                    .seatId(request.getSeatId())
                    .seatNumber(request.getSeatNumber())
                    .reservationStatus(ReservationStatus.PENDING)
                    .concertId(request.getConcertId())
                    .build();

            // 저장 후 반환
            Reservation savedReservation = reservationRepository.save(reservation);

            return PostReservationResponse.from(savedReservation);
        } catch (Exception e) {
            throw new ReservationCreationException();
        }
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

    @Transactional(readOnly = true)
    public Page<ReservationResponseDto> getReservations(Long userId, int page, int size) {
        int adjustedPage = Math.max(page - 1, 0); // 최소값 0으로 설정
        Pageable pageable = PageRequest.of(adjustedPage, size);

        return reservationRepository.findByUserId(userId, pageable).map(
                ReservationResponseDto::from
        );
    }
}
