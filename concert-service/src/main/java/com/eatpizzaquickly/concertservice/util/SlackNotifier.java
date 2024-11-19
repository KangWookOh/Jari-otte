package com.eatpizzaquickly.concertservice.util;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class SlackNotifier {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${webhooks.url}")
    private String webhooksUrl;

    public void sendNotification(String message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("text", message);

        try {
            restTemplate.postForEntity(webhooksUrl, payload, String.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void notifyFailureMessage(Object event, Throwable ex) {
        String message = String.format(
                """
                        [이벤트 발행 실패]
                        - Event: %s
                        - Error: %s
                        """,
                event.toString(),
                ex.getMessage()
        );

        sendNotification(message);
    }

    public void notifyKafkaError(ConsumerRecord<?, ?> consumerRecord, Exception exception) {
        String message = String.format(
                """
                        [Kafka 메시지 처리 실패]
                        - Topic: %s
                        - Partition: %d
                        - Offset: %d
                        - Message: %s
                        - Exception: %s
                        """,
                consumerRecord.topic(),
                consumerRecord.partition(),
                consumerRecord.offset(),
                consumerRecord.value(),
                exception.getMessage()
        );

        sendNotification(message);
    }
}
