package com.eatpizzaquickly.notificationservice.kafka.consumer;

import com.eatpizzaquickly.notificationservice.kafka.dto.CouponEvent;
import com.eatpizzaquickly.notificationservice.service.EmailService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CouponEventConsumer {

    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "coupon-events",
            groupId = "notification-group",
            containerFactory = "KafkaListenerContainerFactory"
    )
    public void listenCouponEvent(String message) {
        try {
            log.debug("수신된 원본 메시지: {}", message);  // 디버그 레벨로 원본 메시지 로깅

            // JSON 메시지를 CouponEvent 객체로 역직렬화
            CouponEvent event = objectMapper.readValue(message, CouponEvent.class);

            // 이메일 주소 유효성 검사
            if (event.getEmail() == null || event.getEmail().trim().isEmpty()) {
                log.warn("이메일 주소가 없거나 유효하지 않습니다. 이메일 발송을 건너뜁니다. 메시지: {}", message);
                return;
            }

            log.info("쿠폰 이벤트 처리 시작 - Email: {}", event.getEmail());

            // notificationMessage가 있으면 사용, 없으면 기본 메시지 생성
            String emailMessage = event.getNotificationMessage() != null ?
                    event.getNotificationMessage() :
                    "쿠폰이 발급되었습니다!";

            // 이메일 발송
            emailService.sendEmail(event.getEmail(), "쿠폰 발급 알림", emailMessage);
            log.info("쿠폰 발급 알림 이메일 발송 완료 - Email: {}", event.getEmail());

        } catch (JsonProcessingException e) {
            log.error("쿠폰 이벤트 JSON 파싱 중 오류 발생. 원본 메시지: {}, 에러: {}", message, e.getMessage());
        } catch (MessagingException e) {
            log.error("이메일 발송 중 오류 발생 - 에러: {}", e.getMessage());
        } catch (Exception e) {
            log.error("쿠폰 이벤트 처리 중 예상치 못한 오류 발생. 메시지: {}, 에러: {}", message, e.getMessage(), e);
        }
    }
}