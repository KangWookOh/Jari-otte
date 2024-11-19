package com.eatpizzaquickly.batchservice.settlement.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class SettlementBatchScheduler {
    private final JobLauncher jobLauncher;
    private final Job settlementBatchJob;

    @Scheduled(fixedDelay = Long.MAX_VALUE, initialDelay = 10000)
    public void runBatchJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("requestDate", LocalDateTime.now().toString())
                    .toJobParameters();

            jobLauncher.run(settlementBatchJob, jobParameters);
            log.info("Coupon batch job completed successfully");
        } catch (Exception e) {
            log.error("Error occurred while running settlement batch job", e);
        }
    }
}
