package com.eatpizzaquickly.concertservice.repository;

import com.eatpizzaquickly.concertservice.dto.response.SeatListResponse;
import com.eatpizzaquickly.concertservice.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Repository
public class ConcertRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedissonClient redissonClient;

    private static final String REQUEST_COUNT_KEY = "concert:requestCount";
    private static final String QUEUE_KEY = "concert:queue";
    private static final String IN_RESERVATION_KEY = "concert:in_reservation";
    private static final int MAX_REQUESTS_PER_SECOND = 100;

    // 조회수 증가
    public void increaseViewCount(Long concertId) {
        RScoredSortedSet<Long> sortedSet = redissonClient.getScoredSortedSet(RedisUtil.getViewCountKey());
        sortedSet.addScore(concertId, 1);
    }

    // 상위 조회수 콘서트 조회
    public List<Long> getTopViewedConcertIds(int limit) {
        RScoredSortedSet<Long> sortedSet = redissonClient.getScoredSortedSet(RedisUtil.getViewCountKey());
        Collection<Long> concertIds = sortedSet.valueRangeReversed(0, limit - 1);
        return new ArrayList<>(concertIds);
    }

    // 예약 가능한 좌석을 Redis에 추가
    public void addAvailableSeats(Long concertId, List<Long> seatIds) {
        RSet<String> availableSeats = redissonClient.getSet(RedisUtil.getAvailableSeatsKey(concertId));
        for (Long seatId : seatIds) {
            availableSeats.add(String.valueOf(seatId));
        }
    }

    // Redis에서 잔여 좌석 수 조회
    public int getAvailableSeatCount(Long concertId) {
        String redisKey = RedisUtil.getAvailableSeatsKey(concertId);
        RSet<String> availableSeats = redissonClient.getSet(redisKey);
        return availableSeats.size();
    }

    // 좌석을 다시 Redis에 추가 (예약 실패 시)
    public void addSeatBackToAvailable(Long concertId, Long seatId) {
        String redisKey = RedisUtil.getAvailableSeatsKey(concertId);
        RSet<String> availableSeats = redissonClient.getSet(redisKey);
        availableSeats.add(String.valueOf(seatId));
    }

    public void reserveSeat(Long concertId, Long seatId) {
        String availableSeatsKey = RedisUtil.getAvailableSeatsKey(concertId);
        String seatValue = String.valueOf(seatId);

        String result = redisTemplate.execute(
                reserveSeatScript(),
                List.of(availableSeatsKey),
                seatValue
        );

        SeatReservationCode.checkReservationResult(SeatReservationCode.find(result));
    }

    private RedisScript<String> reserveSeatScript() {
        String script = """
                if redis.call('SISMEMBER', KEYS[1], ARGV[1]) == 1 then
                    redis.call('SREM', KEYS[1], ARGV[1]) -- 좌석을 Set에서 제거
                    return '1' -- 성공적으로 예약됨
                else
                    return '2' -- 이미 예약된 좌석
                end
                """;
        return RedisScript.of(script, String.class);
    }

    // Redis 에서 좌석 데이터가 있는지 확인
    public boolean hasAvailableSeats(Long concertId) {
        String availableSeatsKey = RedisUtil.getAvailableSeatsKey(concertId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(availableSeatsKey));
    }

    public boolean isRequestLimitExceeded() {
        long currentSecond = getCurrentSecond() / 1000;
        String requestKey = REQUEST_COUNT_KEY + ":" + currentSecond;

        Long requestCount = redisTemplate.opsForValue().increment(requestKey); // 요청 수 증가
        redisTemplate.expire(requestKey, Duration.ofSeconds(1)); // 키 만료시간 1초로 설정

        return requestCount != null && requestCount > MAX_REQUESTS_PER_SECOND;
    }

    public void addToQueue(Long userId) {
        Long timestamp = getCurrentSecond();
        redisTemplate.opsForZSet().add(QUEUE_KEY, String.valueOf(userId), timestamp); // 대기열에 추가
    }

    public Integer getQueuePosition(Long userId) {
        Long position = redisTemplate.opsForZSet().rank(QUEUE_KEY, userId);
        return (position != null) ? position.intValue() + 1 : null;
    }

    public void markInReservation(Long userId) {
        redisTemplate.opsForSet().add(IN_RESERVATION_KEY, String.valueOf(userId));
    }

    public boolean isInReservation(Long userId) {
        return redisTemplate.opsForSet().isMember(IN_RESERVATION_KEY, userId);
    }

    public void removeFromReservation(Long userId) {
        redisTemplate.opsForSet().remove(IN_RESERVATION_KEY, userId);
    }

    public Long getNextUserFromQueue() {
        // 대기열에서 첫 번째 사용자 가져오기
        Set<String> nextUsers = redisTemplate.opsForZSet().range(QUEUE_KEY, 0, 0);
        if (nextUsers == null || nextUsers.isEmpty()) {
            return null; // 대기열이 비어있다면 null 반환
        }

        Long nextUserId = Long.valueOf(nextUsers.iterator().next());

        // 대기열에서 해당 사용자 제거
        redisTemplate.opsForZSet().remove(QUEUE_KEY, nextUserId);

        // "좌석 예매 중" 상태에 사용자 추가
        markInReservation(nextUserId);

        return nextUserId; // 다음 사용자 ID 반환
    }

    // Redis 대기열이 비어 있는지 확인하는 메서드
    public boolean isQueueEmpty() {
        Long queueSize = redisTemplate.opsForZSet().zCard(QUEUE_KEY);
        return queueSize == null || queueSize == 0; // null 인 경우를 처리
    }

    private Long getCurrentSecond() {
        return System.currentTimeMillis() / 1000;
    }
}
