package com.eatpizzaquickly.batchservice.settlement.reader;

import com.eatpizzaquickly.batchservice.common.client.PaymentClient;
import com.eatpizzaquickly.batchservice.settlement.dto.response.PaymentResponseDto;
import com.eatpizzaquickly.batchservice.settlement.entity.PayStatus;
import com.eatpizzaquickly.batchservice.settlement.entity.SettlementStatus;
import com.eatpizzaquickly.batchservice.settlement.entity.TempPayment;
import com.eatpizzaquickly.batchservice.settlement.repository.TempPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.*;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.eatpizzaquickly.batchservice.settlement.common.BatchConstant.CHUNK_SIZE;

@RequiredArgsConstructor
@Component
public class PaymentReader {

    private final PaymentClient paymentClient;
    private final TempPaymentRepository tempPaymentRepository;
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
                        ? executionContext.getInt(CURRENT_PAGE_KEY)
                        : 0;
            }

            @Override
            public PaymentResponseDto read() {
                // 현재 청크가 비었거나 모든 아이템을 처리한 경우 다음 페이지의 데이터를 가져옴
                if (currentChunk.isEmpty()) {
                    currentChunk = paymentClient.getPaymentsByStatus(SettlementStatus.UNSETTLED, PayStatus.PAID, CHUNK_SIZE, currentPage);

                    // 데이터를 모두 처리한 경우 null 반환하여 종료
                    if (currentChunk.isEmpty()) {
                        return null;
                    }

                    // 페이지 증가 후 ExecutionContext에 저장
                    currentPage++;
                    executionContext.putInt(CURRENT_PAGE_KEY, currentPage);
                }

                // 첫 번째 데이터를 반환하고, 이후 제거 (다음 read 호출 시 새로운 데이터 제공)
                return currentChunk.remove(0);
            }
        };
    }

    public RepositoryItemReader<TempPayment> pointAdditionReader() {
        return new RepositoryItemReaderBuilder<TempPayment>()
                .name("pointAdditionReader")
                .repository(tempPaymentRepository)
                .methodName("findBySettlementStatus")
                .arguments(SettlementStatus.PROGRESS)  // 정산 진행 중인 결제 조회
                .pageSize(CHUNK_SIZE)
                .sorts(Collections.singletonMap("id", Sort.Direction.ASC))
                .build();
    }

    public RepositoryItemReader<TempPayment> updatePaymentReader() {
        return new RepositoryItemReaderBuilder<TempPayment>()
                .name("updatePaymentReader")
                .repository(tempPaymentRepository)
                .methodName("findBySettlementStatus")
                .arguments(SettlementStatus.SETTLED)
                .pageSize(CHUNK_SIZE)
                .sorts(Collections.singletonMap("id", Sort.Direction.ASC))
                .build();
    }


}
