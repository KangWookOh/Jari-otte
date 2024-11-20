package com.eatpizzaquickly.batchservice.concert.client;

import com.eatpizzaquickly.batchservice.concert.service.WaitingQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WaitingQueueSubscriber {

    private final WaitingQueueService waitingQueueService;

    @EventListener
    public void onMessage(String concertId) {
        log.info("Received activation message for concertId: {}", concertId);
//        waitingQueueService.processWaitingQueue(Long.valueOf(concertId));
        waitingQueueService.startProcessingQueue(Long.valueOf(concertId));
    }
}
