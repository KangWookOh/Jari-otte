package com.eatpizzaquickly.batchservice.settlement.processor;

import com.eatpizzaquickly.reservationservice.payment.client.ConcertClient;
import com.eatpizzaquickly.reservationservice.payment.entity.HostPoint;
import com.eatpizzaquickly.reservationservice.payment.entity.Payment;
import com.eatpizzaquickly.reservationservice.payment.entity.SettlementStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import static com.eatpizzaquickly.reservationservice.batch.common.BatchConstant.COMMISSION;

@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentProcessor {
    private final ConcertClient concertClient;
    public ItemProcessor<Payment, Payment> settlementSettledProcessor() {
        return payment -> {
            payment.setSettlementStatus(SettlementStatus.SETTLED);
            return payment;
        };
    }

    public ItemProcessor<Payment, HostPoint> pointAdditionProcessor() {
        return payment -> {
            Long concertId = payment.getReservation().getConcertId();
            Long hostId = concertClient.findHostIdByConcertId(concertId);
            Long points = calculatePoints(payment.getAmount()); // 수수료 떼고 정산
            return new HostPoint(hostId, points);
        };
    }

    public ItemProcessor<Payment, Payment> paymentSettleProcessor() {
        return payment -> {
            log.info("Payment {} 정산 진행 중", payment.getPayUid());
            payment.setSettlementStatus(SettlementStatus.PROGRESS);
            return payment;
        };
    }

    Long calculatePoints(Long amount) {
        return (long) (amount * COMMISSION);
    }
}
