package com.eatpizzaquickly.reservationservice.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class SettlementBatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job settlementBatchJob;

    @Scheduled(cron = "0 0 5 * * *", zone = "Asia/Seoul")
    public void runSettlementBatchJob() {
        try {
            jobLauncher.run(settlementBatchJob, new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())  // 각 실행을 고유하게 만들기 위한 파라미터
                    .toJobParameters());
            log.info("Settlement batch job completed successfully");
        } catch (Exception e) {
            log.error("Error occurred while running batch job", e);
        }
    }
}
