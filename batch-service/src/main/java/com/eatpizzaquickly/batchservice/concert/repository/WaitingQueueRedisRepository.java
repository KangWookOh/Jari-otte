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
public class WaitingQueueRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;

    // 활성 대기열 조회
    public List<Long> getActiveConcertIds() {
        // Redis Set 에서 활성화된 콘서트 ID 목록 조회
        Set<String> activeConcertIds = redisTemplate.opsForSet().members("queue:active");

        // Set 이 비어 있는 경우 빈 리스트 반환
        if (activeConcertIds == null || activeConcertIds.isEmpty()) {
            return Collections.emptyList();
        }

        // String 값을 Long 으로 변환하여 반환
        return activeConcertIds.stream()
                .map(Long::valueOf)
                .toList();
    }

    public void removeActiveConcert(Long concertId) {
        redisTemplate.opsForSet().remove("queue:active", String.valueOf(concertId));
    }

    public boolean isQueueEmpty(Long concertId) {
        String queueKey = RedisUtil.getQueueKey(concertId);
        Long queueSize = redisTemplate.opsForZSet().zCard(queueKey);
        return queueSize == null || queueSize == 0; // 크기가 0이면 대기열 비어 있음
    }

    // 대기열에서 사용자 제거
    public void removeFromQueue(Long concertId, List<Long> userIds) {
        String queueKey = RedisUtil.getQueueKey(concertId);
        userIds.forEach(userId -> redisTemplate.opsForZSet().remove(queueKey, String.valueOf(userId)));
    }

    // 대기열에서 사용자 가져오기
    public List<Long> getNextUsersFromQueue(Long concertId, int batchSize) {
        String queueKey = RedisUtil.getQueueKey(concertId);
        Set<String> nextUsers = redisTemplate.opsForZSet().range(queueKey, 0, batchSize - 1);

        if (nextUsers == null || nextUsers.isEmpty()) {
            return Collections.emptyList();
        }

        return nextUsers.stream().map(Long::valueOf).toList();
    }

    public void markInReservation(Long concertId, Long userId) {
        String inReservationKey = RedisUtil.getInReservationKey(concertId);
        redisTemplate.opsForSet().add(inReservationKey, String.valueOf(userId));
    }

    public boolean isQueueActive(Long concertId) {
        // Redis 의 "queue:active" Set 에 콘서트 ID가 있는지 확인
        String activeQueueKey = "queue:active"; // 활성 대기열을 관리하는 Redis 키
        Boolean isActive = redisTemplate.opsForSet().isMember(activeQueueKey, String.valueOf(concertId));

        // null 체크 및 활성 상태 반환
        return Boolean.TRUE.equals(isActive);
    }
}
