package com.eatpizzaquickly.reservationservice.payment.repository;

import com.eatpizzaquickly.reservationservice.payment.dto.response.PaymentSimpleResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.eatpizzaquickly.reservationservice.payment.entity.QPayment.payment;

@RequiredArgsConstructor
public class PaymentQueryDslRepositoryImpl implements PaymentQueryDslRepository {

    private final JPAQueryFactory queryFactory;

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
