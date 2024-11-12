package com.eatpizzaquickly.batchservice.settlement.reader;

import com.eatpizzaquickly.reservationservice.payment.entity.PayStatus;
import com.eatpizzaquickly.reservationservice.payment.entity.Payment;
import com.eatpizzaquickly.reservationservice.payment.entity.SettlementStatus;
import com.eatpizzaquickly.reservationservice.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;

import static com.eatpizzaquickly.reservationservice.batch.common.BatchConstant.CHUNK_SIZE;

@RequiredArgsConstructor
@Component
public class PaymentReader {
    private final PaymentRepository paymentRepository;

    public RepositoryItemReader<Payment> paidPaymentReader() {
        return new RepositoryItemReaderBuilder<Payment>()
                .name("paymentReader")
                .repository(paymentRepository)
                .methodName("getPaidPaymentsOlderThanSevenDays")
                .arguments(PayStatus.PAID, SettlementStatus.UNSETTLED, LocalDateTime.now().minusDays(7))
                .pageSize(CHUNK_SIZE)
                .sorts(Collections.singletonMap("id", Sort.Direction.ASC))
                .build();
    }

    public RepositoryItemReader<Payment> pointAdditionReader() {
        return new RepositoryItemReaderBuilder<Payment>()
                .name("pointAdditionReader")
                .repository(paymentRepository)
                .methodName("findBySettlementStatus")
                .arguments(SettlementStatus.PROGRESS)  // 정산 진행 중인 결제 조회
                .pageSize(CHUNK_SIZE)
                .sorts(Collections.singletonMap("id", Sort.Direction.ASC))
                .build();
    }


}
