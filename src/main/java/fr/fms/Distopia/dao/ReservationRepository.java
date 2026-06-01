package fr.fms.Distopia.dao;

import fr.fms.Distopia.entities.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * JPA repository used to manage {@link Reservation} entities
 * <p>
 * Provides multiples way of retrieving Reservation
 *
 */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * Retrieves a list of Reservation using userId
     *
     * @param userId the user identifier
     * @return the ordered (desc) list of Reservation for the given user
     */
    List<Reservation> findByUserIdOrderByReservedAtDesc(Long userId);

    /**
     * Retrieves a Reservation using userId and seanceId (if it exists)
     *
     * @param userId the user identifier
     * @param seanceId the seance identifier
     * @return an optional of Reservation if user and seance exists
     */
    Optional<Reservation> findByUserIdAndSeanceId(Long userId, Long seanceId);

    /**
     * Retrieves all Reservations using userId and seanceId
     *
     * @param userId the user identifier
     * @param seanceId the seance identifier
     * @return a list of all Reservation for provided params
     */
    List<Reservation> findAllByUserIdAndSeanceId(Long userId, Long seanceId);

    /**
     * Counts reservations linked to the given seance identifiers
     *
     * @param seanceIds the seance identifiers to check
     * @return the number of reservations linked to the given seances
     */
    @Query("""
            SELECT COUNT(r)
            FROM Reservation r
            WHERE r.seance.id IN :seanceIds
            """)
    long countBySeanceIds(@Param("seanceIds") Collection<Long> seanceIds);
}
