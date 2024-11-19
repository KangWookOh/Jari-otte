package com.eatpizzaquickly.concertservice.config;

import com.eatpizzaquickly.concertservice.service.KafkaFailedMessageService;
import com.eatpizzaquickly.concertservice.util.SlackNotifier;
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

                // 슬랙 알림 발송
                slackNotifier.notifyKafkaError(consumerRecord, exception);

                // 실패 메시지 저장
                kafkaFailedMessageService.saveConsumeFailedMessage(consumerRecord, exception);

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
