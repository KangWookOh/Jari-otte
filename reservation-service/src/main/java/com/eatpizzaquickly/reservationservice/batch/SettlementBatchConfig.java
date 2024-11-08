package com.eatpizzaquickly.reservationservice.batch;

import com.eatpizzaquickly.reservationservice.payment.client.UserClient;
import com.eatpizzaquickly.reservationservice.payment.entity.HostPoint;
import com.eatpizzaquickly.reservationservice.payment.entity.Payment;
import com.eatpizzaquickly.reservationservice.payment.entity.SettlementStatus;
import com.eatpizzaquickly.reservationservice.payment.repository.HostPointRepository;
import com.eatpizzaquickly.reservationservice.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
@Slf4j
public class SettlementBatchConfig {
    private static final int CHUNK_SIZE = 500;

    private static final Double COMMISSION = 0.8;
    private final PaymentRepository paymentRepository;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final HostPointRepository hostPointRepository;
    private final UserClient userClient;

    @Bean
    public Job settlementBatchJob() {
        return new JobBuilder("SettlementBatchJob", jobRepository)
                .start(settlementStep())
                .next(hostPointStorageStep())
                .next(pointTransmissionStep())
                .build();
    }

    public Step settlementStep() {
        return new StepBuilder("settlementStep", jobRepository)
                .<Payment, Payment>chunk(CHUNK_SIZE, transactionManager)
                .reader(paidPaymentReader())
                .processor(paymentSettleProcessor())
                .writer(paymentSettleWriter())
                .build();
    }

    private Step hostPointStorageStep() {
        return new StepBuilder("hostPointStorageStep", jobRepository)
                .<Payment, HostPoint>chunk(CHUNK_SIZE, transactionManager)
                .reader(pointAdditionReader())
                .processor(pointAdditionProcessor())
                .writer(hostPointWriter())
                .build();
    }

    private Step pointTransmissionStep() {
        return new StepBuilder("pointAdditionStep", jobRepository)
                .<HostPoint, HostPoint>chunk(CHUNK_SIZE, transactionManager)
                .reader(hostPointReader())
                .processor(pointTransmissionProcessor())
                .writer(hostPointTransmissionWriter())
                .build();
    }

    private RepositoryItemReader<HostPoint> hostPointReader() {
        return new RepositoryItemReaderBuilder<HostPoint>()
                .name("hostPointReader")
                .repository(hostPointRepository)
                .methodName("findAll")
                .pageSize(CHUNK_SIZE)
                .sorts(Collections.singletonMap("id", Sort.Direction.ASC))
                .build();
    }

    private ItemProcessor<HostPoint, HostPoint> pointTransmissionProcessor() {
        return hostPoint -> {
            HashMap<Long, Long> hostPoints = new HashMap<>();
            hostPoints.put(hostPoint.getHostId(), hostPoint.getPoints());

            ResponseEntity<String> response = userClient.addPointsToHost(hostPoints);
            log.info("포인트 추가 응답 STATUS {}", response.getStatusCode());
            return hostPoint;
        };
    }

    private ItemWriter<HostPoint> hostPointTransmissionWriter() {
        return hostPoints -> {
            hostPointRepository.deleteAll(hostPoints);
            log.info("호스트 포인트 전송 완료");
        };
    }


    private ItemWriter<HostPoint> hostPointWriter() {
        return HostPoint -> {
            hostPointRepository.saveAll(HostPoint);
            log.info("중간 결과 저장");
        };
    }

    private RepositoryItemReader<Payment> pointAdditionReader() {
        return new RepositoryItemReaderBuilder<Payment>()
                .name("pointAdditionReader")
                .repository(paymentRepository)
                .methodName("findBySettlementStatus")
                .arguments(SettlementStatus.PROGRESS)  // 정산 진행 중인 결제 조회
                .pageSize(CHUNK_SIZE)
                .sorts(Collections.singletonMap("id", Sort.Direction.ASC))
                .build();
    }

    private ItemProcessor<Payment, HostPoint> pointAdditionProcessor() {
        return payment -> {
            Long hostId = payment.getReservation().getHostId();
            Long points = calculatePoints(payment.getAmount()); // 수수료 떼고 정산
            return new HostPoint(hostId, points);
        };
    }

    private ItemWriter<Payment> pointAdditionWriter() {
        return payments -> {
            paymentRepository.saveAll(payments);
            log.info("포인트 추가");
        };
    }

    public RepositoryItemReader<Payment> paidPaymentReader() {
        return new RepositoryItemReaderBuilder<Payment>()
                .name("paymentReader")
                .repository(paymentRepository)
                .methodName("getPaidPaymentsOlderThanSevenDays")
                .arguments(SettlementStatus.PROGRESS,LocalDateTime.now().minusDays(7))
                .pageSize(CHUNK_SIZE)
                .sorts(Collections.singletonMap("id", Sort.Direction.ASC))
                .build();
    }

    private ItemProcessor<Payment, Payment> paymentSettleProcessor() {
        return payment -> {
            log.info("Payment {} 정산 진행 중", payment.getPayUid());
            payment.setSettlementStatus(SettlementStatus.PROGRESS);
            return payment;
        };
    }

    private ItemWriter<Payment> paymentSettleWriter() {
        return payments -> {
            paymentRepository.saveAll(payments);
            log.info("Payment Settled");
        };
    }

    private Long calculatePoints(Long amount) {
        return (long) (amount * COMMISSION);
    }
}
