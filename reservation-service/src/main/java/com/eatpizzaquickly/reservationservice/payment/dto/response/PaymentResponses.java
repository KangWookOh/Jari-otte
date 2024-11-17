package com.eatpizzaquickly.reservationservice.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponses {
    private String orderId;
    private String paymentKey;
    private Long amount;
    private String status;
}
