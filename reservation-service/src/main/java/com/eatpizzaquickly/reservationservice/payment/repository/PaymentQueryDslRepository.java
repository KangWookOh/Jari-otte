package com.eatpizzaquickly.reservationservice.payment.repository;


import com.eatpizzaquickly.reservationservice.payment.dto.response.PaymentResponseDto;
import com.eatpizzaquickly.reservationservice.payment.entity.PayStatus;
import com.eatpizzaquickly.reservationservice.payment.entity.SettlementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.eatpizzaquickly.reservationservice.payment.dto.response.PaymentSimpleResponse;

import java.time.LocalDateTime;

public interface PaymentQueryDslRepository {
    Page<PaymentResponseDto> getPaymentsByStatusAfterId(SettlementStatus settlementStatus, PayStatus payStatus, LocalDateTime sevenDaysAgo, Long currentOffset, int chunk);
    Page<PaymentSimpleResponse> getPaymentByUserId(Long userId, Pageable pageable);
}
