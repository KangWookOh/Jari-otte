package com.eatpizzaquickly.concertservice.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisCachePublisher {

    private final RedisTemplate<String, String> redisTemplate;

    public void publishCacheUpdate(Long concertId) {
        redisTemplate.convertAndSend("cache-update-channel", String.valueOf(concertId));
        log.info("Published cache update event for concertId: {}", concertId);
    }

    public void publishActivateWaitingQueue(Long concertId) {
        // Redis Set 에 "대기열 활성화" 상태로 추가
        redisTemplate.opsForSet().add("queue:active", String.valueOf(concertId));

        // Redis Pub/Sub 로 대기열 활성화 알림 발행
        redisTemplate.convertAndSend("queue:activate", String.valueOf(concertId));
    }
}
