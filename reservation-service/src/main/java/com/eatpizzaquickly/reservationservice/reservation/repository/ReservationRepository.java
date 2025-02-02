package com.eatpizzaquickly.reservationservice.reservation.repository;

import com.eatpizzaquickly.reservationservice.reservation.entity.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<List<Reservation>> findByConcertId(Long concertId);

    Page<Reservation> findByUserId(Long userId, Pageable pageable);
}
