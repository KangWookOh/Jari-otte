package com.eatpizzaquickly.reservationservice.payment.repository;


import com.eatpizzaquickly.reservationservice.payment.dto.response.PaymentResponseDto;
import com.eatpizzaquickly.reservationservice.payment.entity.PayStatus;
import com.eatpizzaquickly.reservationservice.payment.entity.Payment;
import com.eatpizzaquickly.reservationservice.payment.entity.QPayment;
import com.eatpizzaquickly.reservationservice.payment.entity.SettlementStatus;
import com.eatpizzaquickly.reservationservice.payment.dto.response.PaymentSimpleResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

import java.util.List;

import static com.eatpizzaquickly.reservationservice.payment.entity.QPayment.payment;

@RequiredArgsConstructor
public class PaymentQueryDslRepositoryImpl implements PaymentQueryDslRepository {

    private final JPAQueryFactory queryFactory;


    public Page<PaymentResponseDto> getPaymentsByStatus(
            SettlementStatus settlementStatus,
            PayStatus payStatus,
            LocalDateTime before,
            int currentOffset,
            int chunk) {
        Pageable pageable = PageRequest.of(0, chunk);

        List<PaymentResponseDto> results = queryFactory
                .select(Projections.constructor(
                        PaymentResponseDto.class,
                        payment.id,
                        payment.settlementStatus,
                        payment.payStatus,
                        payment.amount,
                        payment.reservation.concertId))
                .from(payment)
                .where(
                        payment.settlementStatus.eq(settlementStatus),
                        payment.payStatus.eq(payStatus),
                        payment.paidAt.before(before),
                        payment.id.gt(currentOffset))
                .orderBy(payment.id.asc())
                .limit(chunk)
                .fetch();

        return new PageImpl<>(results, pageable, chunk);
    }

    @Override
    public Page<PaymentSimpleResponse> getPaymentByUserId(Long userId, Pageable pageable) {
        List<PaymentSimpleResponse> results = queryFactory
                .select(Projections.fields(PaymentSimpleResponse.class,
                        payment.amount,
                        payment.payInfo,
                        payment.payStatus,
                        payment.reservation.concertId))
                .from(payment)
                .leftJoin(payment.reservation)
                .where(
                        eqUserId(userId)
                )
                .orderBy(payment.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long count = queryFactory.select(payment.count())
                .from(payment)
                .leftJoin(payment.reservation)
                .where(
                        eqUserId(userId)
                )
                .fetchOne();

        return new PageImpl<>(results, pageable, count);
    }

    private BooleanExpression eqUserId(Long userId) {
        return userId != null ? payment.reservation.userId.eq(userId) : null;
    }
}
