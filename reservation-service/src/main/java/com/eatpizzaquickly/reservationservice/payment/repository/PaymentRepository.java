package com.eatpizzaquickly.reservationservice.payment.repository;

import com.eatpizzaquickly.reservationservice.payment.entity.PayStatus;
import com.eatpizzaquickly.reservationservice.payment.entity.Payment;
import com.eatpizzaquickly.reservationservice.payment.entity.SettlementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>,PaymentQueryDslRepository {
    Optional<Payment> findByPayUid(String orderId);

    Optional<Payment> findByPaymentKey(String paymentKey);

    @Query("SELECT p FROM Payment p WHERE p.payStatus = :paid AND p.settlementStatus = :status AND p.createdAt < :sevenDays")
    Page<Payment> getPaidPaymentsOlderThanSevenDays(@Param("paid") PayStatus paid, @Param("status") SettlementStatus status, @Param("sevenDays") LocalDateTime sevenDays, Pageable pageable);

    Page<Payment> findBySettlementStatus(SettlementStatus settlementStatus, Pageable pageable);
}
