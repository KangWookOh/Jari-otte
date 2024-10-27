package com.eatpizzaquickly.concertservice.service;

import com.eatpizzaquickly.concertservice.client.reservation.PostReservationRequest;
import com.eatpizzaquickly.concertservice.client.reservation.PostReservationResponse;
import com.eatpizzaquickly.concertservice.client.reservation.ReservationServiceClient;
import com.eatpizzaquickly.concertservice.dto.SeatDto;
import com.eatpizzaquickly.concertservice.dto.request.SeatReservationRequest;
import com.eatpizzaquickly.concertservice.dto.response.SeatListResponse;
import com.eatpizzaquickly.concertservice.dto.response.SeatReservationResponse;
import com.eatpizzaquickly.concertservice.entity.Concert;
import com.eatpizzaquickly.concertservice.entity.Seat;
import com.eatpizzaquickly.concertservice.exception.BadRequestException;
import com.eatpizzaquickly.concertservice.exception.NotFoundException;
import com.eatpizzaquickly.concertservice.repository.ConcertRepository;
import com.eatpizzaquickly.concertservice.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class SeatService {

    private final ConcertRepository concertRepository;
    private final SeatRepository seatRepository;
    private final ReservationServiceClient reservationServiceClient;

    public SeatListResponse findSeatList(Long concertId) {
        List<Seat> seatList = seatRepository.findByConcertId(concertId);
        List<SeatDto> seatDtoList = seatList.stream().map(SeatDto::from).toList();
        return SeatListResponse.of(seatDtoList);
    }

    //TODO: 중복된 유저가 예매하는 경우 검증
    @Transactional
    public void reserveSeat(Long userId, Long concertId, Long seatId, SeatReservationRequest request) {
        Concert concert = concertRepository.findById(concertId).orElseThrow(
                () -> new NotFoundException("콘서트가 존재하지 않습니다."));

        Seat seat = seatRepository.findById(seatId).orElseThrow(
                () -> new NotFoundException("좌석이 존재하지 않습니다."));

        PostReservationRequest postReservationRequest = PostReservationRequest.builder()
                .price(request.getPrice())
                .concertId(concertId)
                .userId(userId)
                .seatId(seatId)
                .build();

        PostReservationResponse postReservationResponse = reservationServiceClient.createReservation(postReservationRequest);

        if (postReservationResponse.getStatus().isEmpty()) {
            throw new BadRequestException();
        }

        seat.changeReserved(true);
        concert.decreaseSeatCount(); //TODO: 동시성 이슈
    }
}
