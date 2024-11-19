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
        redisTemplate.convertAndSend("cache-update-channel", concertId.toString());
        log.info("Published cache update event for concertId: {}", concertId);
    }
}
