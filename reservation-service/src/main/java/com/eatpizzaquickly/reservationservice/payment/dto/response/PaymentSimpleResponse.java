package com.eatpizzaquickly.reservationservice.payment.dto.response;

import com.eatpizzaquickly.reservationservice.payment.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentSimpleResponse {
    private Long amount;
    private String payInfo;
    private String payStatus;
    private Long concertId;

    public static PaymentSimpleResponse from(Payment payment){
        return new PaymentSimpleResponse(
                payment.getAmount(),
                payment.getPayInfo(),
                payment.getPayStatus().name(),
                payment.getReservation().getConcertId()
        );
    }
}
