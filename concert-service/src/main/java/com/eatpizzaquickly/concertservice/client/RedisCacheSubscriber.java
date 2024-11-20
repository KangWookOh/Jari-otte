package com.eatpizzaquickly.concertservice.client;

import com.eatpizzaquickly.concertservice.service.ConcertCacheService;
import com.eatpizzaquickly.concertservice.service.ConcertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class RedisCacheSubscriber {

    private final ConcertService concertService;
    private final ConcertCacheService concertCacheService;

    @EventListener
    public void onMessage(String concertId) {
        log.info("Received cache update event for concertId: {}", concertId);
        concertService.findConcert(Long.parseLong(concertId));
        concertCacheService.putTopConcertsCache();
    }
}
