package com.eatpizzaquickly.reservationservice.batch.common;

import com.eatpizzaquickly.reservationservice.batch.processor.HostPointProcessor;
import com.eatpizzaquickly.reservationservice.batch.processor.PaymentProcessor;
import com.eatpizzaquickly.reservationservice.batch.reader.HostPointReader;
import com.eatpizzaquickly.reservationservice.batch.reader.PaymentReader;
import com.eatpizzaquickly.reservationservice.batch.writer.HostPointWriter;
import com.eatpizzaquickly.reservationservice.batch.writer.PaymentWriter;
import com.eatpizzaquickly.reservationservice.payment.dto.request.HostPointRequestDto;
import com.eatpizzaquickly.reservationservice.payment.entity.HostPoint;
import com.eatpizzaquickly.reservationservice.payment.entity.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import static com.eatpizzaquickly.reservationservice.batch.common.BatchConstant.CHUNK_SIZE;

@Configuration
@RequiredArgsConstructor
@EnableBatchProcessing
@Slf4j
public class SettlementBatchConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final HostPointProcessor hostPointProcessor;
    private final PaymentProcessor paymentProcessor;
    private final HostPointReader hostPointReader;
    private final PaymentReader paymentReader;
    private final HostPointWriter hostPointWriter;
    private final PaymentWriter paymentWriter;


    @Bean
    public Job settlementBatchJob() {
        return new JobBuilder("SettlementBatchJob", jobRepository)
                .start(settlementStep())
                .next(hostPointStorageStep())
                .next(pointTransmissionStep())
                .next(settlementSettledStep())
                .build();
    }

    public Step settlementStep() {
        return new StepBuilder("settlementStep", jobRepository)
                .<Payment, Payment>chunk(CHUNK_SIZE, transactionManager)
                .reader(paymentReader.paidPaymentReader())
                .processor(paymentProcessor.paymentSettleProcessor())
                .writer(paymentWriter.paymentSettleWriter())
                .build();
    }

    private Step hostPointStorageStep() {
        return new StepBuilder("hostPointStorageStep", jobRepository)
                .<Payment, HostPoint>chunk(CHUNK_SIZE, transactionManager)
                .reader(paymentReader.pointAdditionReader())
                .processor(paymentProcessor.pointAdditionProcessor())
                .writer(hostPointWriter.hostPointWriter())
                .build();
    }

    private Step pointTransmissionStep() {
        return new StepBuilder("pointAdditionStep", jobRepository)
                .<HostPoint, HostPointRequestDto>chunk(CHUNK_SIZE, transactionManager)
                .reader(hostPointReader.hostPointReader())
                .processor(hostPointProcessor.pointTransmissionProcessor())
                .writer(hostPointWriter.hostPointTransmissionWriter())
                .build();
    }

    private Step settlementSettledStep() {
        return new StepBuilder("settlementSettledStep", jobRepository)
                .<Payment, Payment>chunk(CHUNK_SIZE, transactionManager)
                .reader(paymentReader.pointAdditionReader())
                .processor(paymentProcessor.settlementSettledProcessor())
                .writer(paymentWriter.settlementSettledWriter())
                .build();
    }

}
