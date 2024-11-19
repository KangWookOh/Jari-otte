package com.eatpizzaquickly.batchservice.concert.scheduler;

import com.eatpizzaquickly.batchservice.concert.service.ConcertService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ConcertScheduler {

    private final ConcertService concertService;

    @Scheduled(cron = "0 0 0 * * SUN") // 매주 일요일 자정 실행
    public void scheduleResetTopConcerts() {
        concertService.resetTopConcerts();
    }
}
