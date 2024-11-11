package com.eatpizzaquickly.concertservice.service;

import com.eatpizzaquickly.concertservice.client.KafkaEventProducer;
import com.eatpizzaquickly.concertservice.dto.SeatDto;
import com.eatpizzaquickly.concertservice.dto.SeatReservationEvent;
import com.eatpizzaquickly.concertservice.dto.request.SeatReservationRequest;
import com.eatpizzaquickly.concertservice.dto.response.ApiResponse;
import com.eatpizzaquickly.concertservice.dto.response.SeatListResponse;
import com.eatpizzaquickly.concertservice.entity.Concert;
import com.eatpizzaquickly.concertservice.entity.Seat;
import com.eatpizzaquickly.concertservice.exception.NotFoundException;
import com.eatpizzaquickly.concertservice.exception.detail.InvalidReservationFlowException;
import com.eatpizzaquickly.concertservice.exception.detail.RequestLimitExceededException;
import com.eatpizzaquickly.concertservice.repository.ConcertRedisRepository;
import com.eatpizzaquickly.concertservice.repository.ConcertRepository;
import com.eatpizzaquickly.concertservice.repository.QueueRedisRepository;
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
    private final ConcertRedisRepository concertRedisRepository;
    private final QueueRedisRepository queueRedisRepository;
    private final KafkaEventProducer kafkaEventProducer;

    public SeatListResponse findSeatList(Long concertId, Long userId) {
        // 사용자가 "좌석 예매 중" 상태에 있는지 먼저 확인
        if (queueRedisRepository.isInReservation(concertId, userId)) {
            return fetchSeatList(concertId);
        }

        if (queueRedisRepository.isRequestLimitExceeded(concertId)) {
            queueRedisRepository.addToQueue(concertId, userId);
            throw new RequestLimitExceededException(concertId);
        }

        // "좌석 예매 중" 마크
        queueRedisRepository.markInReservation(concertId, userId);

        // Redis 에 좌석 데이터가 없으면 DB 에서 다시 로드
        if (!concertRedisRepository.hasAvailableSeats(concertId)) {
            reloadSeatsFromDatabase(concertId);
        }

        return fetchSeatList(concertId);
    }

    @Transactional
    public void reserveSeat(Long userId, Long concertId, Long seatId, SeatReservationRequest request) {
        if (!queueRedisRepository.isInReservation(concertId, userId)) {
            throw new InvalidReservationFlowException();
        }

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

            //TODO : 스케쥴링 혹은 특점 시점에만 반영하기
//            int availableSeatCount = concertRedisRepository.getAvailableSeatCount(concertId);
//            concert.updateSeatCount(availableSeatCount);

            SeatReservationEvent reservationEvent = SeatReservationEvent.builder()
                    .userId(userId)
                    .seatId(seatId)
                    .concertId(concertId)
                    .price(request.getPrice())
                    .build();

            kafkaEventProducer.produceSeatReservationEvent(reservationEvent);

            queueRedisRepository.removeFromReservation(concertId, userId);

            processNextUserInQueue(concertId);

        } catch (Exception e) {
            concertRedisRepository.addSeatBackToAvailable(concertId, seatId);
            queueRedisRepository.markInReservation(concertId, userId);
            throw new RuntimeException("좌석 예약 중 오류가 발생했습니다.");
        }
    }


    private void reloadSeatsFromDatabase(Long concertId) {
        List<Seat> availableSeats = seatRepository.findAvailableSeatsByConcertId(concertId);
        List<Long> availableSeatIds = availableSeats.stream().map(Seat::getId).toList();
        concertRedisRepository.addAvailableSeats(concertId, availableSeatIds);
    }

    private void processNextUserInQueue(Long concertId) {
        if (queueRedisRepository.isQueueEmpty(concertId)) {
            return; // 대기열이 비어있으면 아무 작업도 수행하지 않음
        }

        Long nextUserId = queueRedisRepository.getNextUserFromQueue(concertId);
        if (nextUserId != null) {
            queueRedisRepository.markInReservation(concertId, nextUserId);
        }
    }

    private SeatListResponse fetchSeatList(Long concertId) {
        // Redis 에 좌석 데이터가 없으면 DB 에서 다시 로드
        if (!concertRedisRepository.hasAvailableSeats(concertId)) {
            reloadSeatsFromDatabase(concertId);
        }

        List<Seat> seatList = seatRepository.findByConcertId(concertId);
        List<SeatDto> seatDtoList = seatList.stream().map(SeatDto::from).toList();
        return SeatListResponse.of(seatDtoList);
    }
}
