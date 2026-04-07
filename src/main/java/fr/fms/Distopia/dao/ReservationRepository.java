package fr.fms.Distopia.dao;

import fr.fms.Distopia.entities.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUserIdOrderByReservedAtDesc(Long userId);
}
