package com.eatpizzaquickly.concertservice.service;

import com.eatpizzaquickly.concertservice.dto.SeatReservationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class KafkaEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void produceSeatReservationEvent(SeatReservationEvent event) {
        kafkaTemplate.send("seat.reservation.created", event);
    }

}
