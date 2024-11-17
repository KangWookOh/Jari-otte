package com.eatpizzaquickly.reservationservice.payment.dto.response;

import com.eatpizzaquickly.reservationservice.payment.entity.PayStatus;
import com.eatpizzaquickly.reservationservice.payment.entity.Payment;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)

public class PaymentSimpleResponse {
    private Long amount;
    private String payInfo;
    private PayStatus payStatus;
    private Long concertId;

    public static PaymentSimpleResponse from(Payment payment){
        return new PaymentSimpleResponse(
                payment.getAmount(),
                payment.getPayInfo(),
                payment.getPayStatus(),
                payment.getReservation().getConcertId()
        );
    }
}
