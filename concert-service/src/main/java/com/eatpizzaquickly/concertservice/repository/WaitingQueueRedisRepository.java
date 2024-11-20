package com.eatpizzaquickly.concertservice.repository;

import com.eatpizzaquickly.concertservice.exception.detail.RedisException;
import com.eatpizzaquickly.concertservice.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Repository
public class WaitingQueueRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private static final int MAX_REQUESTS_PER_SECOND = 100;

    public boolean isRequestLimitExceeded(Long concertId) {
        String requestKey = RedisUtil.getRequestCountKey(concertId);

        Long requestCount = redisTemplate.opsForValue().increment(requestKey); // 요청 수 증가

        if (requestCount == null) {
            throw new RedisException();
        }

        if(requestCount == 1){
            redisTemplate.expire(requestKey, Duration.ofSeconds(1)); // TTL 1초 설정
        }

        return requestCount > MAX_REQUESTS_PER_SECOND;
    }

    public void addToWaitingQueue(Long concertId, Long userId) {
        String queueKey = RedisUtil.getQueueKey(concertId);
        Long timestamp = getCurrentSecond();
        if (redisTemplate.opsForZSet().rank(queueKey, String.valueOf(userId)) == null) { // 대기열에 추가
            redisTemplate.opsForZSet().add(queueKey, String.valueOf(userId), timestamp);
        }
    }

    public Integer getQueuePosition(Long concertId, Long userId) {
        String queueKey = RedisUtil.getQueueKey(concertId);
        Long position = redisTemplate.opsForZSet().rank(queueKey, String.valueOf(userId));

        if (position == null) {
            return null;
        }

        return position.intValue() + 1;
    }

    public void markInReservation(Long concertId, Long userId) {
        String inReservationKey = RedisUtil.getInReservationKey(concertId);
        redisTemplate.opsForSet().add(inReservationKey, String.valueOf(userId));
    }

    public boolean isInReservation(Long concertId, Long userId) {
        String inReservationKey = RedisUtil.getInReservationKey(concertId);
        Boolean isMember = redisTemplate.opsForSet().isMember(inReservationKey, String.valueOf(userId));
        return isMember != null && isMember;
    }

    public void removeFromReservation(Long concertId, Long userId) {
        String inReservationKey = RedisUtil.getInReservationKey(concertId);
        redisTemplate.opsForSet().remove(inReservationKey, String.valueOf(userId));
    }

    public boolean isWaitingQueueActive(Long concertId) {
        // Redis 에서 ZSet 의 크기를 가져오기
        Long queueSize = redisTemplate.opsForZSet().zCard(RedisUtil.getQueueKey(concertId));

        return queueSize != null && queueSize > 0;
    }

    private Long getCurrentSecond() {
        return System.currentTimeMillis() / 1000;
    }
}
