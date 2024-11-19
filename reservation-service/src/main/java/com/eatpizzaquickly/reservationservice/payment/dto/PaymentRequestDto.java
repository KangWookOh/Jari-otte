package com.eatpizzaquickly.reservationservice.payment.dto;

import com.eatpizzaquickly.reservationservice.common.enums.PayStatus;
import com.eatpizzaquickly.reservationservice.common.enums.SettlementStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentRequestDto {
    private Long id;
    private SettlementStatus settlementStatus;
    private PayStatus payStatus;
    private Long amount;
}
