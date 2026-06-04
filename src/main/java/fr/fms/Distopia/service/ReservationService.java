package fr.fms.Distopia.service;

import fr.fms.Distopia.dao.ReservationRepository;
import fr.fms.Distopia.dao.SeanceRepository;
import fr.fms.Distopia.dao.UserRepository;
import fr.fms.Distopia.entities.Reservation;
import fr.fms.Distopia.entities.Seance;
import fr.fms.Distopia.entities.User;
import fr.fms.Distopia.exceptions.NoSeatsAvailableException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service responsible for reservation business operations.
 * <p>
 * It retrieves user reservations and creates bookings while protecting seat
 * updates with a pessimistic lock.
 */
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final SeanceRepository seanceRepository;

    public  ReservationService(ReservationRepository reservationRepository, UserRepository userRepository,
                               SeanceRepository seanceRepository) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.seanceRepository = seanceRepository;
    }

    private static final Logger logger = LoggerFactory.getLogger(ReservationService.class);

    //--------reservations d'un utilisateur--------
    /**
     * Retrieves all reservations made by a specific user
     * <p>
     * The results are ordered chronologically by the reservation date in descending order
     * (the most recent reservations are returned first)
     *
     * @param userId the unique identifier of the user
     * @return a list of {@link Reservation} objects belonging to the specified user
     */
    public List<Reservation> getByUser(Long userId){
        return reservationRepository.findByUserIdOrderByReservedAtDesc(userId);
    }

    //------------------créer une réservation---------------
    /**
     * Creates or updates a reservation for a specific seance and user<p>
     * <strong>Concurrency handling:</strong> the seance is retrieved with a pessimistic
     * write lock to prevent two concurrent reservations from overselling the same seats
     * <p>
     * <strong>Business rules:</strong> a reservation is refused if the requested quantity
     * is invalid, if the seance is inactive, if the seance is already past, if the movie
     * is soft-deleted or if there are not enough seats available.
     * <p>
     * If the user already has a reservation for the same seance, the existing reservation
     * quantity is increased instead of creating a duplicate reservation.
     *
     * @param seanceId the unique identifier of the seance to book
     * @param userId   the unique identifier of the user making the reservation
     * @param quantity the number of seats being reserved
     * @return the created or updated {@link Reservation} entity
     * @throws java.util.NoSuchElementException if either the seance or the user is not found
     * @throws IllegalStateException if the seance is no longer available for reservation
     * @throws IllegalArgumentException if the requested quantity is less than one
     * @throws NoSeatsAvailableException if the requested quantity is greater than the available seats
     */
    @Transactional
    public Reservation createReservation(Long seanceId, Long userId, int quantity){
        Seance seance = seanceRepository.findByIdForUpdate(seanceId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();

        validateReservationRequest(seance, quantity);

        List<Reservation> existingList = reservationRepository.findAllByUserIdAndSeanceId(userId, seanceId);

        seance.setAvailableSeats(seance.getAvailableSeats() - quantity);
        seanceRepository.save(seance);

        if (!existingList.isEmpty()){
            Reservation resa = existingList.get(0);

            if (existingList.size() > 1){
                reservationRepository.deleteAll(existingList.subList(1, existingList.size()));
            }

            resa.setQuantity(resa.getQuantity() + quantity);
            return reservationRepository.save(resa);
        }

        Reservation reservation = new Reservation();
        reservation.setReservedAt(LocalDateTime.now());
        reservation.setSeance(seance);
        reservation.setUser(user);
        reservation.setQuantity(quantity);

        logger.info("Reservation created: userId={}, seanceId={}, quantity={}", userId, seanceId, quantity);

        return reservationRepository.save(reservation);
    }

    /**
     * Validates that a reservation request can be accepted for the selected seance<p>
     * A reservation is considered valid only when the requested quantity is positive,
     * the seance is active, the seance is scheduled in the future, the related movie
     * is not soft-deleted and enough seats are still available.
     *
     * @param seance the seance selected for reservation
     * @param quantity the requested number of seats
     * @throws IllegalArgumentException if the requested quantity is less than one
     * @throws IllegalStateException if the seance or movie is no longer available
     * @throws NoSeatsAvailableException if the requested quantity exceeds available seats
     */
    private void validateReservationRequest(Seance seance, int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("La quantité doit être supérieure à 0");
        }
        if (!seance.isActive() || seance.getDateTime().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Cette séance n'est plus disponible à la réservation");
        }
        if (seance.getMovie() == null || seance.getMovie().isDeleted()) {
            throw new IllegalStateException("Ce film n'est plus disponible à la réservation");
        }
        if (seance.getAvailableSeats() < quantity){
            throw new NoSeatsAvailableException("Seulement " + seance.getAvailableSeats() + " places disponibles");
        }
    }

    /**
     * Checks whether a user already has a reservation
     * for a specific seance
     *
     * @param userId the user identifier
     * @param seanceId the seance identifier
     * @return true if the reservation already exists, otherwise false
     */
    public boolean existsByUserAndSeance(Long userId, Long seanceId) {
        return reservationRepository.findByUserIdAndSeanceId(userId, seanceId).isPresent();
    }

    /**
     * Retrieves the movie identifier associated with a seance
     * @param seanceId the seance identifier
     * @return the movie identifier, or null if the seance does not exist
     */
    public Long getMovieIdBySeance(Long seanceId) {
        return seanceRepository.findById(seanceId)
                .map(s -> s.getMovie().getId())
                .orElse(null);
    }

    /**
     * Retrieves the cinema identifier associated with a seance
     * @param seanceId the seance identifier
     * @return the cinema identifier, or null if the seance does not exist
     */
    public Long getCinemaIdBySeance(Long seanceId) {
        return seanceRepository.findById(seanceId)
                .map(s -> s.getCinema().getId())
                .orElse(null);
    }
}
