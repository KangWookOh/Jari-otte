package com.eatpizzaquickly.concertservice.repository;

import com.eatpizzaquickly.concertservice.dto.SeatDto;
import com.eatpizzaquickly.concertservice.util.JsonUtil;
import com.eatpizzaquickly.concertservice.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Repository
public class ConcertRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedissonClient redissonClient;
    private final JsonUtil jsonUtil;

    // 조회수 증가
    public void increaseViewCount(Long concertId) {
        RScoredSortedSet<Long> sortedSet = redissonClient.getScoredSortedSet(RedisUtil.getTopConcertsKey());
        sortedSet.addScore(concertId, 1);
    }

    // 상위 조회수 콘서트 조회
    public List<Long> getTopConcertsIds(int limit) {
        RScoredSortedSet<Long> sortedSet = redissonClient.getScoredSortedSet(RedisUtil.getTopConcertsKey());
        Collection<Long> concertIds = sortedSet.valueRangeReversed(0, limit - 1);
        return new ArrayList<>(concertIds);
    }

    public boolean isTopConcert(Long concertId) {
        String topConcertsKey = RedisUtil.getTopConcertsKey();
        Double score = redisTemplate.opsForZSet().score(topConcertsKey, concertId.toString());
        return score != null;
    }

    // 예약 가능한 좌석을 Redis 에 추가
    public void addAvailableSeats(Long concertId, List<SeatDto> seatDtoList) {
        RSet<String> availableSeats = redissonClient.getSet(RedisUtil.getAvailableSeatsKey(concertId));
        for (SeatDto seatDto : seatDtoList) {
            String seatJson = jsonUtil.toJson(seatDto);
            availableSeats.add(seatJson);
        }
    }

    // Redis 에서 잔여 좌석 수 조회
    public int getAvailableSeatCount(Long concertId) {
        String redisKey = RedisUtil.getAvailableSeatsKey(concertId);
        RSet<String> availableSeats = redissonClient.getSet(redisKey);
        return availableSeats.size();
    }

    public void addSeatBackToAvailable(Long concertId, SeatDto seatDto) {
        String redisKey = RedisUtil.getAvailableSeatsKey(concertId);
        RSet<String> availableSeats = redissonClient.getSet(redisKey);
        String seatJson = jsonUtil.toJson(seatDto);
        availableSeats.add(seatJson);
    }

    public void reserveSeat(Long concertId, SeatDto seatDto) {
        String availableSeatsKey = RedisUtil.getAvailableSeatsKey(concertId);
        String seatJson = jsonUtil.toJson(seatDto);
        log.info("seatJson: {}", seatJson);
        String result = redisTemplate.execute(
                reserveSeatScript(),
                List.of(availableSeatsKey),
                seatJson
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

    public void resetTopConcerts() {
        String topConcertsKey = RedisUtil.getTopConcertsKey();
        redissonClient.getScoredSortedSet(topConcertsKey).delete();
    }

    public Set<String> getAvailableSeats(Long concertId) {
        String redisKey = RedisUtil.getAvailableSeatsKey(concertId);

        RSet<String> availableSeats = redissonClient.getSet(redisKey);

        return availableSeats.readAll();
    }
}
