package com.eatpizzaquickly.batchservice.concert.service;

import com.eatpizzaquickly.batchservice.concert.repository.WaitingQueueRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class WaitingQueueService {

    private final WaitingQueueRedisRepository waitingQueueRedisRepository;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1); // 단일 스레드 스케줄러

    private static final int WAITING_BATCH_SIZE = 10;

    public void processWaitingQueue(Long concertId) {
        try {
            log.info("Starting queue processing for concertId: {}", concertId);

            // 대기열이 활성 상태이고 비어있지 않은 동안 반복
            while (waitingQueueRedisRepository.isQueueActive(concertId)) {
                processQueue(concertId);

                if (waitingQueueRedisRepository.isQueueEmpty(concertId)) {
                    waitingQueueRedisRepository.removeActiveConcert(concertId);
                    log.info("Removed concertId {} from active queue list.", concertId);
                    break;
                }

                Thread.sleep(3000); // 3초 대기
            }

            log.info("Queue processing completed for concertId: {}", concertId);
        } catch (InterruptedException e) {
            log.error("Queue processing interrupted for concertId: {}", concertId, e);
            Thread.currentThread().interrupt(); // 인터럽트 상태 복원
        } catch (Exception e) {
            log.error("Failed to process queue for concertId: {}", concertId, e);
        }
    }

    public void startProcessingQueue(Long concertId) {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                processQueue(concertId);
            } catch (Exception e) {
                log.error("Error processing queue for concertId: {}", concertId, e);
            }
        }, 0, 3, TimeUnit.SECONDS); // 0초 후 시작, 3초 간격으로 실행
    }

    //    private void processQueue(Long concertId) {
//         대기열에서 사용자 가져오기
//        List<Long> nextUsers = waitingQueueRedisRepository.getNextUsersFromQueue(concertId, WAITING_BATCH_SIZE);
//
//        for (Long userId : nextUsers) {
//             "예매 중"으로 이동
//            waitingQueueRedisRepository.markInReservation(concertId, userId);
//
//             대기열에서 제거
//            waitingQueueRedisRepository.removeFromQueue(concertId, List.of(userId));
//        }
//    }
    private void processQueue(Long concertId) {
        if (waitingQueueRedisRepository.isQueueEmpty(concertId)) {
            waitingQueueRedisRepository.removeActiveConcert(concertId);
            log.info("Removed concertId {} from active queue list.", concertId);
            scheduler.shutdown(); // 대기열이 비어있으면 스케줄러 중단
            return;
        }

        List<Long> nextUsers = waitingQueueRedisRepository.getNextUsersFromQueue(concertId, WAITING_BATCH_SIZE);
        for (Long userId : nextUsers) {
            waitingQueueRedisRepository.markInReservation(concertId, userId);
            waitingQueueRedisRepository.removeFromQueue(concertId, List.of(userId));
        }
    }
}
