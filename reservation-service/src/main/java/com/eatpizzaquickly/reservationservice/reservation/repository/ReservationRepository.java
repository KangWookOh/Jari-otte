package com.eatpizzaquickly.reservationservice.reservation.repository;

import com.eatpizzaquickly.reservationservice.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findByConcertId(Long concertId);
}
