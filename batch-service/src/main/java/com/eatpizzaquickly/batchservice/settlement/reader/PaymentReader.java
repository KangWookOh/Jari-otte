package com.eatpizzaquickly.batchservice.settlement.reader;

import com.eatpizzaquickly.batchservice.common.client.PaymentClient;
import com.eatpizzaquickly.batchservice.settlement.dto.response.PaymentResponseDto;
import com.eatpizzaquickly.batchservice.settlement.entity.PayStatus;
import com.eatpizzaquickly.batchservice.settlement.entity.SettlementStatus;
import com.eatpizzaquickly.batchservice.settlement.entity.TempPayment;
import com.eatpizzaquickly.batchservice.settlement.repository.TempPaymentRepository;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.eatpizzaquickly.batchservice.settlement.common.BatchConstant.CHUNK_SIZE;

@RequiredArgsConstructor
@Component
@Slf4j
public class PaymentReader {

    private final PaymentClient paymentClient;
    private final TempPaymentRepository tempPaymentRepository;
    private final EntityManagerFactory entityManagerFactory;
    private static final String CURRENT_PAGE_KEY = "currentPage";
    private static final String CURRENT_INDEX_KEY = "currentIndex";

    public ItemReader<PaymentResponseDto> paidPaymentReader() {
        return new ItemReader<PaymentResponseDto>() {

            private List<PaymentResponseDto> currentChunk = new ArrayList<>();
            private ExecutionContext executionContext;
            private int currentPage;

            @BeforeStep
            public void saveStepExecution(StepExecution stepExecution) {
                this.executionContext = stepExecution.getExecutionContext();
                this.currentPage = executionContext.containsKey(CURRENT_PAGE_KEY)
                        ? executionContext.getInt(CURRENT_PAGE_KEY) : 0;

                log.info("Initial Current Page: {}", currentPage);
            }

            @Override
            public PaymentResponseDto read() {
                // 현재 청크가 비었거나 모든 아이템을 처리한 경우 다음 페이지의 데이터를 가져옴
                if (currentChunk.isEmpty()) {
                    log.info("Fetching new chunk of payments at page {}", currentPage);
                    currentChunk = paymentClient.getPaymentsByStatus(SettlementStatus.UNSETTLED, PayStatus.PAID, CHUNK_SIZE, currentPage);

                    // 데이터를 모두 처리한 경우 null 반환하여 종료
                    if (currentChunk.isEmpty()) {
                        log.info("No more data available at page {}", currentPage);
                        return null;
                    }

                    // 페이지 증가 후 ExecutionContext에 저장
                    currentPage++;
                    executionContext.putInt(CURRENT_PAGE_KEY, currentPage);
                    log.info("Updated Current Page to: {}", currentPage);
                }

                // 첫 번째 데이터를 반환하고, 이후 제거 (다음 read 호출 시 새로운 데이터 제공)
                PaymentResponseDto nextPayment = currentChunk.remove(0);
                log.info("Processing PaymentResponseDto: {}", nextPayment);
                return nextPayment;
            }
        };
    }


    @Bean
    public JpaPagingItemReader<TempPayment> pointAdditionReader() {
        return createReader("pointAdditionReader", SettlementStatus.PROGRESS);
    }

    @Bean
    public JpaPagingItemReader<TempPayment> updatePaymentReader() {
        return createReader("updatePaymentReader", SettlementStatus.SETTLED);
    }

    @Bean
    public JpaPagingItemReader<TempPayment> settlementSettledReader() {
        return createReader("settlementSettledReader",SettlementStatus.PROGRESS);
    }

    private JpaPagingItemReader<TempPayment> createReader(String name, SettlementStatus status) {
        return new JpaPagingItemReaderBuilder<TempPayment>()
                .name(name)
                .entityManagerFactory(entityManagerFactory)
                .pageSize(CHUNK_SIZE)
                .queryString("SELECT t FROM TempPayment t WHERE t.settlementStatus = :status ORDER BY t.id ASC")
                .parameterValues(Collections.singletonMap("status", status))
                .build();
    }



}
