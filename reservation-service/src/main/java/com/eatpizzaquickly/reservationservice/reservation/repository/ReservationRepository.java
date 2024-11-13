package com.eatpizzaquickly.reservationservice.reservation.repository;

import com.eatpizzaquickly.reservationservice.reservation.entity.Reservation;
import com.eatpizzaquickly.reservationservice.reservation.entity.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findByConcertId(Long concertId);

    Page<Reservation> findByUserIdAndStatus(Long userId, ReservationStatus pending, Pageable pageable);
}
