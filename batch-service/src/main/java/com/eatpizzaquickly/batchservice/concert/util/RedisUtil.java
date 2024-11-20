package com.eatpizzaquickly.batchservice.concert.util;

public class RedisUtil {
    public static String getQueueKey(Long concertId) {
        return "concert:%s:queue".formatted(concertId);
    }

    public static String getInReservationKey(Long concertId) {
        return "concert:%s:in_reservation".formatted(concertId);
    }
}
