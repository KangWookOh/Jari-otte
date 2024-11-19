package com.eatpizzaquickly.reservationservice.payment.dto.request;

import com.eatpizzaquickly.reservationservice.common.enums.PayStatus;
import com.eatpizzaquickly.reservationservice.common.enums.SettlementStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequestDto {
    private Long id;
    private SettlementStatus settlementStatus;
    private PayStatus payStatus;
    private Long amount;
}
