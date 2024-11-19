package com.eatpizzaquickly.batchservice.concert.util;

public class RedisUtil {
    public static String getQueueKey(Long concertId) {
        return "concert:%s:queue".formatted(concertId);
    }
}
