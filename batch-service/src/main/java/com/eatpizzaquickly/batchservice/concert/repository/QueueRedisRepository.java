package com.eatpizzaquickly.batchservice.concert.repository;

import com.eatpizzaquickly.batchservice.concert.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class QueueRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;

    // 활성 대기열 조회
    public List<Long> getActiveConcertIds() {
        // Redis Set에서 활성화된 콘서트 ID 목록 조회
        Set<String> activeConcertIds = redisTemplate.opsForSet().members("queue:active");

        // Set이 비어 있는 경우 빈 리스트 반환
        if (activeConcertIds == null || activeConcertIds.isEmpty()) {
            return Collections.emptyList();
        }

        // String 값을 Long으로 변환하여 반환
        return activeConcertIds.stream()
                .map(Long::valueOf)
                .toList();
    }

    public void removeActiveConcert(Long concertId) {
        redisTemplate.opsForSet().remove("queue:active", String.valueOf(concertId));
    }

    public boolean isQueueEmpty(Long concertId) {
        String queueKey = RedisUtil.getQueueKey(concertId);
        Long queueSize = redisTemplate.opsForZSet().zCard(queueKey); // Sorted Set 크기 확인
        return queueSize == null || queueSize == 0; // 크기가 0이면 대기열 비어 있음
    }
}
