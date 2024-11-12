package com.eatpizzaquickly.batchservice.settlement.repository;

import com.eatpizzaquickly.batchservice.settlement.entity.SettlementStatus;
import com.eatpizzaquickly.batchservice.settlement.entity.TempPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TempPaymentRepository extends JpaRepository<TempPayment, Long> {
    Page<TempPayment> findBySettlementStatus(SettlementStatus settlementStatus, Pageable pageable);

    List<TempPayment> findByPaymentIdIn(List<Long> paymentId);
}
