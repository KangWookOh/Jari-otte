package com.eatpizzaquickly.reservationservice.common.config;

import com.eatpizzaquickly.reservationservice.common.util.SlackNotifier;
import com.eatpizzaquickly.reservationservice.reservation.exception.ReservationCreationException;
import com.eatpizzaquickly.reservationservice.reservation.service.KafkaFailedMessageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.backoff.FixedBackOff;
import org.springframework.web.client.HttpServerErrorException;

@Slf4j
@RequiredArgsConstructor
@Component
public class CustomKafkaErrorHandler {

    private final KafkaFailedMessageService kafkaFailedMessageService;
    private final SlackNotifier slackNotifier;

    public DefaultErrorHandler getErrorHandler() {
        long interval = 3000L;
        long maxAttempts = 3L;

        FixedBackOff fixedBackOff = new FixedBackOff(interval, maxAttempts);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler((consumerRecord, exception) -> {
            try {
                log.error("재시도 초과. message: {}, exception: {}", consumerRecord.value(), exception.getMessage());

                // 실패 메시지 저장
                kafkaFailedMessageService.saveFailedMessage(consumerRecord, exception);

                Throwable cause = exception.getCause() != null ? exception.getCause() : exception;

                if (cause instanceof ReservationCreationException) {
                    // 보상 트랜잭션 이벤트 발행 및 슬랙 알림 발송
                    kafkaFailedMessageService.produceReservationCompensationEvent(consumerRecord);
                }else {
                    // 보상 트랜잭션 대상이 아님.
                    slackNotifier.notifyKafkaError(consumerRecord, cause);
                }

            } catch (Exception ex) {
                log.error("Kafka 에러 핸들링 실패: {}", ex.getMessage());
            }

        }, fixedBackOff);

        errorHandler.setAckAfterHandle(true);

        errorHandler.addRetryableExceptions(
                HttpServerErrorException.class
        );

        errorHandler.addNotRetryableExceptions(
                JsonProcessingException.class,
                DataIntegrityViolationException.class,
                IllegalArgumentException.class
        );
        return errorHandler;
    }
}