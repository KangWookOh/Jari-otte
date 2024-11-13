package com.eatpizzaquickly.reservationservice.payment.repository;

import com.eatpizzaquickly.reservationservice.payment.dto.response.PaymentSimpleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentQueryDslRepository {
    Page<PaymentSimpleResponse> getPaymentByUserId(Long userId, Pageable pageable);
}
