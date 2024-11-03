package com.eatpizzaquickly.notificationservice.kafka.consumer;

import com.eatpizzaquickly.notificationservice.kafka.dto.PaymentEvent;
import com.eatpizzaquickly.notificationservice.service.EmailService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final EmailService emailService;
    private final ObjectMapper objectMapper = new ObjectMapper(); // ObjectMapper 인스턴스 생성

    @KafkaListener(
            topics = "payment-events",
            groupId = "notification-group",
            containerFactory = "KafkaListenerContainerFactory"  // 이름 변경
    )
    public void listenPaymentEvent(String message) {
        try {
            log.debug("수신된 원본 메시지: {}", message);  // 디버그 레벨로 원본 메시지 로깅

            // JSON 메시지를 PaymentEvent 객체로 역직렬화
            PaymentEvent event = objectMapper.readValue(message, PaymentEvent.class);
            log.info("결제 이벤트 처리 시작 - PaymentID: {}, Email: {}, Amount: {}",
                    event.getPaymentId(),
                    event.getEmail(),
                    event.getAmount());

            // notificationMessage가 있으면 사용, 없으면 기본 메시지 생성
            String emailMessage = event.getNotificationMessage() != null ?
                    event.getNotificationMessage() :
                    String.format(
                            "결제가 %s 되었습니다.\n결제 ID: %d\n결제 금액: %d원",
                            event.getPaymentStatus(),
                            event.getPaymentId(),
                            event.getAmount()
                    );

            // 이메일 발송
            emailService.sendEmail(event.getEmail(), "결제 알림", emailMessage);
            log.info("결제 알림 이메일 발송 완료 - PaymentID: {}", event.getPaymentId());

        } catch (JsonProcessingException e) {
            log.error("결제 이벤트 처리 중 JSON 역직렬화 오류 발생. 원본 메시지: {}, 에러: {}", message, e.getMessage());
        } catch (MessagingException e) {
            log.error("이메일 발송 중 오류 발생 - 에러: {}", e.getMessage());
        }
    }
}