package com.eatpizzaquickly.concertservice.service;

import com.eatpizzaquickly.concertservice.client.reservation.PostReservationRequest;
import com.eatpizzaquickly.concertservice.client.reservation.PostReservationResponse;
import com.eatpizzaquickly.concertservice.client.reservation.ReservationServiceClient;
import com.eatpizzaquickly.concertservice.dto.SeatDto;
import com.eatpizzaquickly.concertservice.dto.request.SeatReservationRequest;
import com.eatpizzaquickly.concertservice.dto.response.SeatListResponse;
import com.eatpizzaquickly.concertservice.entity.Concert;
import com.eatpizzaquickly.concertservice.entity.Seat;
import com.eatpizzaquickly.concertservice.exception.BadRequestException;
import com.eatpizzaquickly.concertservice.exception.NotFoundException;
import com.eatpizzaquickly.concertservice.repository.ConcertRedisRepository;
import com.eatpizzaquickly.concertservice.repository.ConcertRepository;
import com.eatpizzaquickly.concertservice.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
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
    private final ConcertRedisRepository concertRedisRepository;
    private final RedissonClient redissonClient;

    public SeatListResponse findSeatList(Long concertId) {
        List<Seat> seatList = seatRepository.findByConcertId(concertId);
        List<SeatDto> seatDtoList = seatList.stream().map(SeatDto::from).toList();
        return SeatListResponse.of(seatDtoList);
    }

    //TODO: 중복된 유저가 예매하는 경우 검증
    @Transactional
    public void reserveSeat(Long userId, Long concertId, Long seatId, SeatReservationRequest request) {

        // Redis 의 Lua script 를 이용한 원자적 좌석 예약 처리
        concertRedisRepository.reserveSeat(concertId, seatId);

        try {
            // Seat DB 조회 및 예약 상태 변경
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new NotFoundException("좌석이 존재하지 않습니다."));
            seat.changeReserved(true);

            // Concert 조회 및 잔여 좌석 수 감소
            Concert concert = concertRepository.findById(concertId)
                    .orElseThrow(() -> new NotFoundException("콘서트가 존재하지 않습니다."));

            String redisKey = "concert:" + concertId + ":available_seats";
            RSet<String> availableSeats = redissonClient.getSet(redisKey);

            concert.updateSeatCount(availableSeats.size());

            //TODO: 비동기적으로 처리할 것
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
        } catch (Exception e) {
            String redisKey = "concert:" + concertId + ":available_seats";
            RSet<String> availableSeats = redissonClient.getSet(redisKey);
            availableSeats.add(String.valueOf(seatId));
            throw new RuntimeException("좌석 예약 중 오류가 발생했습니다.");
        }
    }
}
