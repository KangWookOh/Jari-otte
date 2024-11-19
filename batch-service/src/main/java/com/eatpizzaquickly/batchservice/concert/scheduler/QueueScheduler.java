package com.eatpizzaquickly.batchservice.concert.scheduler;

import com.eatpizzaquickly.batchservice.common.client.ConcertClient;
import com.eatpizzaquickly.batchservice.concert.repository.QueueRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RequiredArgsConstructor
@Component
public class QueueScheduler {

    private final QueueRedisRepository queueRedisRepository;
    private final ConcertClient concertClient;

    @Scheduled(fixedRate = 3000)
    public void processQueues() {
        // Redis 에서 활성 대기열 콘서트 ID 조회
        List<Long> activeConcertIds = queueRedisRepository.getActiveConcertIds();

        if (activeConcertIds.isEmpty()) {
            log.info("No active queues found. Scheduler will not process.");
            return;
        }
        // 병렬 처리
        ExecutorService executorService = Executors.newFixedThreadPool(activeConcertIds.size());

        for (Long concertId : activeConcertIds) {
            executorService.submit(() -> processConcertQueue(concertId));
        }

        executorService.shutdown();
    }

    private void processConcertQueue(Long concertId) {
        try {
            // 대기열 처리 요청
            concertClient.processWaitingQueue(concertId);
            log.info("Processed queue for concertId: {}", concertId);

            // 대기열이 비어 있으면 활성 리스트에서 제거
            if (queueRedisRepository.isQueueEmpty(concertId)) {
                queueRedisRepository.removeActiveConcert(concertId);
                log.info("Removed concertId {} from active queue list.", concertId);
            }
        } catch (Exception e) {
            log.error("Failed to process queue for concertId: {}", concertId, e);
        }
    }
}
