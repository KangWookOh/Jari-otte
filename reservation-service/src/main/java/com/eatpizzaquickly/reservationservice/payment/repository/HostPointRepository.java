package com.eatpizzaquickly.reservationservice.payment.repository;

import com.eatpizzaquickly.reservationservice.payment.entity.HostPoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HostPointRepository extends JpaRepository<HostPoint,Long> {
    Page<HostPoint> findAll(Pageable pageable);
}
