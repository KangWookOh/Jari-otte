package com.eatpizzaquickly.batchservice.settlement.writer;


import com.eatpizzaquickly.batchservice.common.client.PaymentClient;
import com.eatpizzaquickly.batchservice.settlement.dto.request.PaymentRequestDto;
import com.eatpizzaquickly.batchservice.settlement.entity.TempPayment;
import com.eatpizzaquickly.batchservice.settlement.repository.TempPaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.ServiceUnavailableException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentWriter {

    private final PaymentClient paymentClient;
    private final TempPaymentRepository tempPaymentRepository;

    public ItemWriter<TempPayment> tempPaymentWriter() {
        return tempPaymentRepository::saveAll;
    }

    @Transactional
    public ItemWriter<PaymentRequestDto> paymentWriter() {
        return payments -> {
            ResponseEntity<String> response = paymentClient.updatePayments((List<PaymentRequestDto>) payments.getItems());

            if (response.getStatusCode().is2xxSuccessful()) {
                List<Long> paymentId = payments.getItems().stream()
                        .map(PaymentRequestDto::getId)
                        .toList();

                List<TempPayment> tempPayments = tempPaymentRepository.findByPaymentIdIn(paymentId);
                tempPaymentRepository.deleteAll();
            } else {
                log.error("Payment Update fail : {}", response.getStatusCode());
                throw new ServiceUnavailableException("Payment Update Fail");
            }
        };
    }
}
