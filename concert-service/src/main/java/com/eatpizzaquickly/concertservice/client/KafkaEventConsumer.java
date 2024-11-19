package com.eatpizzaquickly.concertservice.client;

import com.eatpizzaquickly.concertservice.dto.event.ReservationCompensationEvent;
import com.eatpizzaquickly.concertservice.service.SeatService;
import com.eatpizzaquickly.concertservice.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaEventConsumer {
    private final SeatService seatService;
    private final JsonUtil jsonUtil;

    @KafkaListener(
            topics = "${spring.kafka.topic.reservation-created-compensation}", groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeSeatReservationCreatedCompensationEvent(String message) {
        log.info("보상 트랜잭션 이벤트 수신: {}", message);
        ReservationCompensationEvent event = jsonUtil.toObject(message, ReservationCompensationEvent.class);
        seatService.compensateReservation(event);
    }
}
