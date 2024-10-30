package com.eatpizzaquickly.concertservice.util;

public class RedisUtil {

    public static String getAvailableSeatsKey(long concertId) {
        return "concert:%s:available_seats".formatted(concertId);
    }

    public static String getViewCountKey() {
        return "concert:view_count";
    }
}
