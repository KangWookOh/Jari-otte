package com.eatpizzaquickly.concertservice.client;

import com.eatpizzaquickly.concertservice.dto.SeatReservationEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class KafkaEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.kafka.topic.reservation.created}")
    private String seatReservationCreatedTopic;

    public void produceSeatReservationEvent(SeatReservationEvent event) throws JsonProcessingException {
        String message = objectMapper.writeValueAsString(event);
        kafkaTemplate.send(seatReservationCreatedTopic, message);
    }

}
