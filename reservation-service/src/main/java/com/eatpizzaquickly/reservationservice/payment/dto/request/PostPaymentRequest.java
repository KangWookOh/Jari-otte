package com.eatpizzaquickly.reservationservice.payment.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostPaymentRequest {
    private Long reservationId;
    private Long amount;
    private String payInfo;
}
