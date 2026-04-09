package fr.fms.Distopia.dao;

import fr.fms.Distopia.entities.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The JPA Repository of reservation
 */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUserIdOrderByReservedAtDesc(Long userId);
    boolean existsByUserIdAndSeanceId(Long userId, Long seanceId);
}
