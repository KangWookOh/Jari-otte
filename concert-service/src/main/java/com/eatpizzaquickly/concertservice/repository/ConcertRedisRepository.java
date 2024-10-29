package com.eatpizzaquickly.concertservice.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class ConcertRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public void reserveSeat(Long concertId, Long seatId) {
        String availableSeatsKey = "concert:" + concertId + ":available_seats";
        String seatKey = String.valueOf(seatId);

        String result = redisTemplate.execute(
                reserveSeatScript(),
                List.of(availableSeatsKey),
                seatKey
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

}
