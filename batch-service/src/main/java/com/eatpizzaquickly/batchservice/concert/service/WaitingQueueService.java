package com.eatpizzaquickly.batchservice.concert.service;

import com.eatpizzaquickly.batchservice.concert.repository.WaitingQueueRedisRepository;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class WaitingQueueService {
    private final WaitingQueueRedisRepository waitingQueueRedisRepository;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    private final Map<Long, ScheduledFuture<?>> queueTasks = new ConcurrentHashMap<>();

    private static final int WAITING_BATCH_SIZE = 1;


    public void startProcessingQueue(Long concertId) {
        if (queueTasks.containsKey(concertId)) {
            log.warn("이미 대기열 진행 작업 중입니다. concertId: {}", concertId);
            return;
        }

        ScheduledFuture<?> task = scheduler.scheduleAtFixedRate(
                () -> processQueueSafely(concertId),
                0, 3, TimeUnit.SECONDS
        );
        queueTasks.put(concertId, task);
        log.info("대기열 진행 작업을 시작했습니다. concertId: {}", concertId);
    }

    private void processQueueSafely(Long concertId) {
        try {
            if (waitingQueueRedisRepository.isQueueEmpty(concertId)) {
                stopProcessingQueue(concertId);
                log.info("대기열이 존재하지 않기에 작업을 중단합니다. concertId: {}", concertId);
                return;
            }

            processQueue(concertId);
        } catch (Exception e) {
            log.error("대기열 진행 작업중 예외가 발생했습니다. concertId: {}", concertId, e);
        }
    }

    private void processQueue(Long concertId) {
        List<Long> nextUsers = waitingQueueRedisRepository.getNextUsersFromQueue(concertId, WAITING_BATCH_SIZE);

        if (!nextUsers.isEmpty()) {
            try {
                for (Long userId : nextUsers) {
                    waitingQueueRedisRepository.markInReservation(concertId, userId);
                    waitingQueueRedisRepository.removeFromQueue(concertId, List.of(userId));
                }

                log.debug("{}명의 사용자를 진행시켰습니다. concertId: {}", nextUsers.size(), concertId);
            } catch (Exception e) {
                log.error("대기열 진행 작업중 예외가 발생했습니다. concertId: {}", concertId, e);
            }
        }
    }

    public void stopProcessingQueue(Long concertId) {
        ScheduledFuture<?> task = queueTasks.remove(concertId);
        if (task != null) {
            task.cancel(false);
            log.info("대기열 진행 작업을 중단합니다. concertId: {}", concertId);
        }
    }

    @PreDestroy
    public void shutdown() {
        queueTasks.forEach((concertId, task) -> task.cancel(false));
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
