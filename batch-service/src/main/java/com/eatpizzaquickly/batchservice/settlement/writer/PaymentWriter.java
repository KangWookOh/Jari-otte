package com.eatpizzaquickly.batchservice.settlement.writer;

import com.eatpizzaquickly.reservationservice.payment.entity.Payment;
import com.eatpizzaquickly.reservationservice.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentWriter {
    private final PaymentRepository paymentRepository;

    public ItemWriter<Payment> pointAdditionWriter() {
        return payments -> {
            paymentRepository.saveAll(payments);
            log.info("포인트 추가");
        };
    }

    public ItemWriter<Payment> settlementSettledWriter() {
        return payments -> {
            paymentRepository.saveAll(payments);
            log.info("정산 완료로 변경");
        };
    }

    public ItemWriter<Payment> paymentSettleWriter() {
        return payments -> {
            paymentRepository.saveAll(payments);
            log.info("Payment Settled");
        };
    }
}
