package com.eatpizzaquickly.concertservice.client;

import com.eatpizzaquickly.concertservice.dto.SeatReservationEvent;
import com.eatpizzaquickly.concertservice.exception.detail.EventPublishingException;
import com.eatpizzaquickly.concertservice.util.SlackNotifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Slf4j
@Component
public class KafkaEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final SlackNotifier slackNotifier;

    @Value("${spring.kafka.topic.reservation-created}")
    private String seatReservationCreatedTopic;

    public void produceSeatReservationEvent(SeatReservationEvent event){
        produceEvent(seatReservationCreatedTopic, event);
    }

    private void produceEvent(String topic, Object event) {
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, event);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("이벤트 발행 실패: {}, Exception: {}", event.toString(), ex.getMessage());
                slackNotifier.notifyFailureMessage(event, ex);
                throw new EventPublishingException();
            }
        });
    }
}
