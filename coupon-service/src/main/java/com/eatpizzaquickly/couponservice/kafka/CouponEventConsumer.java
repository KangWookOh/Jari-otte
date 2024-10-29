package com.eatpizzaquickly.couponservice.kafka;

import com.eatpizzaquickly.couponservice.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponEventConsumer {
    private final CouponService couponService;
    @KafkaListener(topics = "${spring.kafka.topic.coupon-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeCouponEvent(CouponEvent event) {
        log.info("Received coupon event: {}", event);

        try {
            switch (event.getEventType()) {
                case "SINGLE_ISSUE":
                    couponService.issueCouponToUser(event.getUserId(), event.getCouponCode());
                    break;
                case "BULK_ISSUE":
                    couponService.issueCouponToAllUsers(event.getCouponId());
                    break;
                default:
                    log.warn("Unknown event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing coupon event: {}", event, e);
            // 에러 처리 (재시도 로직 또는 DLQ로 이동)
            throw e;
        }
    }

}
