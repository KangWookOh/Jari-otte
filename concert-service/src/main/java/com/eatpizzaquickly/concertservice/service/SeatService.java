package com.eatpizzaquickly.concertservice.service;

import com.eatpizzaquickly.concertservice.client.KafkaEventProducer;
import com.eatpizzaquickly.concertservice.dto.event.ReservationCompensationEvent;
import com.eatpizzaquickly.concertservice.dto.SeatDto;
import com.eatpizzaquickly.concertservice.dto.SeatReservationEvent;
import com.eatpizzaquickly.concertservice.dto.request.SeatReservationRequest;
import com.eatpizzaquickly.concertservice.dto.response.AvailableSeatCountResponse;
import com.eatpizzaquickly.concertservice.dto.response.ConcertDetailResponse;
import com.eatpizzaquickly.concertservice.dto.response.SeatListResponse;
import com.eatpizzaquickly.concertservice.entity.Concert;
import com.eatpizzaquickly.concertservice.entity.Seat;
import com.eatpizzaquickly.concertservice.exception.NotFoundException;
import com.eatpizzaquickly.concertservice.exception.detail.CompensateReservationFailureException;
import com.eatpizzaquickly.concertservice.exception.detail.InvalidReservationFlowException;
import com.eatpizzaquickly.concertservice.exception.detail.RequestLimitExceededException;
import com.eatpizzaquickly.concertservice.repository.ConcertRedisRepository;
import com.eatpizzaquickly.concertservice.repository.ConcertRepository;
import com.eatpizzaquickly.concertservice.repository.WaitingQueueRedisRepository;
import com.eatpizzaquickly.concertservice.repository.SeatRepository;
import com.eatpizzaquickly.concertservice.util.JsonUtil;
import com.eatpizzaquickly.concertservice.util.SlackNotifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class SeatService {

    private final ConcertRepository concertRepository;
    private final SeatRepository seatRepository;
    private final ConcertRedisRepository concertRedisRepository;
    private final WaitingQueueRedisRepository waitingQueueRedisRepository;
    private final KafkaEventProducer kafkaEventProducer;
    private final SlackNotifier slackNotifier;
    private final JsonUtil jsonUtil;
    private final ConcertCacheService concertCacheService;

    public SeatListResponse findSeatList(Long concertId, Long userId) {
        //TODO: CacheService 에서 호출해서 캐싱할것
//        Concert concert = concertRepository.findByIdWithVenue(concertId).orElseThrow(NotFoundException::new);
        ConcertDetailResponse concert = concertCacheService.findConcertWithVenueCache(concertId);
        Integer venueSeatCount = concert.getSeatCount();

        // 이미 "예매 중" 상태인 사용자라면 바로 좌석 정보를 조회
        if (waitingQueueRedisRepository.isInReservation(concertId, userId)) {
            return fetchSeatList(concertId, venueSeatCount);
        }

        // 대기열 활성화 여부 확인
        if (waitingQueueRedisRepository.isQueueActive(concertId)) {
            handleActiveQueue(concertId, userId);
        }

        // 대기열이 비활성화된 경우, 초당 요청 제한 확인
        if (waitingQueueRedisRepository.isRequestLimitExceeded(concertId)) {
            waitingQueueRedisRepository.addToQueue(concertId, userId);
            throw new RequestLimitExceededException(concertId);
        }

        // "예매 중" 상태로 사용자 등록
        waitingQueueRedisRepository.markInReservation(concertId, userId);

        // Redis 에 좌석 데이터가 없으면 DB 에서 다시 로드
        if (!concertRedisRepository.hasAvailableSeats(concertId)) {
            reloadSeatsFromDatabase(concertId);
        }

        return fetchSeatList(concertId, venueSeatCount);
    }

    public AvailableSeatCountResponse getAvailableSeatCount(Long concertId) {
        // Redis 에 좌석 데이터가 없으면 DB 에서 다시 로드
        if (!concertRedisRepository.hasAvailableSeats(concertId)) {
            reloadSeatsFromDatabase(concertId);
        }

        int availableSeatCount = concertRedisRepository.getAvailableSeatCount(concertId);
        return AvailableSeatCountResponse.of(availableSeatCount);
    }

    @Transactional
    public void reserveSeat(Long userId, Long concertId, SeatReservationRequest request) {
        if (!waitingQueueRedisRepository.isInReservation(concertId, userId)) {
            throw new InvalidReservationFlowException();
        }

        SeatDto seatDto = SeatDto.from(request);

        // Redis 의 Lua script 를 이용한 원자적 좌석 예약 처리
        concertRedisRepository.reserveSeat(concertId, seatDto);

        try {
            // Seat DB 조회 및 예약 상태 변경
            Seat seat = seatRepository.findById(request.getSeatId())
                    .orElseThrow(() -> new NotFoundException("좌석이 존재하지 않습니다."));
            seat.changeReserved(true);

            SeatReservationEvent reservationEvent = SeatReservationEvent.builder()
                    .userId(userId)
                    .seatId(request.getSeatId())
                    .seatNumber(seat.getSeatNumber())
                    .concertId(concertId)
                    .price(request.getPrice())
                    .build();

            // "예매 중" 상태 제거
            waitingQueueRedisRepository.removeFromReservation(concertId, userId);

            // 좌석 예매 이벤트 발행
            kafkaEventProducer.produceSeatReservationEvent(reservationEvent);

        } catch (Exception e) {
            concertRedisRepository.addSeatBackToAvailable(concertId, seatDto);
            waitingQueueRedisRepository.markInReservation(concertId, userId);
            throw new RuntimeException("좌석 예약 중 오류가 발생했습니다.");
        }
    }


    private void reloadSeatsFromDatabase(Long concertId) {
        List<Seat> availableSeats = seatRepository.findAvailableSeatsByConcertId(concertId);
        List<SeatDto> seatDtoList = availableSeats.stream().map(SeatDto::from).toList();
        concertRedisRepository.addAvailableSeats(concertId, seatDtoList);
    }

    private SeatListResponse fetchSeatList(Long concertId, Integer venueSeatCount) {
        // Redis 에 좌석 데이터가 없으면 DB 에서 다시 로드
        if (!concertRedisRepository.hasAvailableSeats(concertId)) {
            reloadSeatsFromDatabase(concertId);
        }

        // 캐시된 좌석 정보를 한 번에 가져오기
        Map<Integer, SeatDto> availableSeatsMap = getCachedSeatsMap(concertId);

        // 배열 기반 최적화 (성능 최상)
        SeatDto[] seatDtoArray = new SeatDto[venueSeatCount];
        for (int seatNumber = 0; seatNumber < venueSeatCount; seatNumber++) {
            seatDtoArray[seatNumber] = availableSeatsMap.getOrDefault(
                    seatNumber + 1,
                    SeatDto.builder()
                            .seatNumber(seatNumber + 1)
                            .isReserved(true)
                            .build()
            );
        }

        return SeatListResponse.of(Arrays.asList(seatDtoArray));
    }

    // Redis 상태를 DB로 동기화

    @Transactional
    public void syncAvailableSeatsToDatabase(Long concertId) {
        int availableSeatCount = concertRedisRepository.getAvailableSeatCount(concertId);

        // DB 에서 Concert 엔티티 조회
        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new NotFoundException("콘서트를 찾을 수 없습니다."));

        // 잔여 좌석 수 반영
        concert.updateSeatCount(availableSeatCount);
        concertRepository.save(concert); // 업데이트된 좌석 수 저장
    }
    @Transactional
    public void compensateReservation(ReservationCompensationEvent event) {
        Long concertId = event.getConcertId();
        Long seatId = event.getSeatId();
        Long userId = event.getUserId();

        log.info("보상 트랜잭션 시작 - concertId: {}, seatId: {}, userId: {}", concertId, seatId, userId);

        try {

            // DB 상태 복구
            Seat seat = seatRepository.findById(seatId).orElseThrow(() -> new NotFoundException("해당 좌석이 존재하지 않습니다."));
            seat.changeReserved(false);

            // Redis 상태 복구
            concertRedisRepository.addSeatBackToAvailable(concertId, SeatDto.from(seat));
            waitingQueueRedisRepository.markInReservation(concertId, userId);

            log.info("보상 트랜잭션 완료 - concertId: {}, seatId: {}, userId: {}", concertId, seatId, userId);
            slackNotifier.notifyCompensateReservationSuccess(event);
        } catch (Exception e) {
            log.info("보상 트랜잭션 실패 - concertId: {}, seatId: {}, userId: {}", concertId, seatId, userId);
            throw new CompensateReservationFailureException();
        }
    }

    private void handleActiveQueue(Long concertId, Long userId) {
        if (!waitingQueueRedisRepository.isInReservation(concertId, userId)) {
            waitingQueueRedisRepository.addToQueue(concertId, userId);
            waitingQueueRedisRepository.addActiveConcert(concertId);
            throw new RequestLimitExceededException(concertId);
        }
    }

    private Map<Integer, SeatDto> getCachedSeatsMap(Long concertId) {
        Set<String> availableSeats = Optional.ofNullable(concertRedisRepository.getAvailableSeats(concertId))
                .orElseGet(() -> {
                    reloadSeatsFromDatabase(concertId);
                    return concertRedisRepository.getAvailableSeats(concertId);
                });

        // 한 번의 변환으로 Map 생성
        return availableSeats.stream()
                .map(seatJson -> jsonUtil.toObject(seatJson, SeatDto.class))
                .collect(Collectors.toMap(
                        SeatDto::getSeatNumber,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));
    }
}
