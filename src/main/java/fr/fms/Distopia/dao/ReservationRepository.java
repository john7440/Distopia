package fr.fms.Distopia.dao;

import fr.fms.Distopia.entities.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * The JPA Repository of reservation
 */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUserIdOrderByReservedAtDesc(Long userId);
    Optional<Reservation> findByUserIdAndSeanceId(Long userId, Long seanceId);
    List<Reservation> findAllByUserIdAndSeanceId(Long userId, Long seanceId);
}
