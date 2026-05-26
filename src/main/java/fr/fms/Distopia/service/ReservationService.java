package fr.fms.Distopia.service;

import fr.fms.Distopia.dao.ReservationRepository;
import fr.fms.Distopia.dao.SeanceRepository;
import fr.fms.Distopia.dao.UserRepository;
import fr.fms.Distopia.entities.Reservation;
import fr.fms.Distopia.entities.Seance;
import fr.fms.Distopia.entities.User;
import fr.fms.Distopia.exceptions.NoSeatsAvailableException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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
     * Creates a new reservation for a specific seance and user
     * <p>
     * <strong>Concurrency Handling:</strong> This method is transactional. It retrieves the
     * {@link Seance} using a pessimistic lock ({@code findByIdForUpdate}) to ensure that
     * multiple concurrent booking requests do not result in overselling seats
     * <p>
     * If the requested quantity exceeds the currently available seats, the transaction
     * is aborted and a {@link NoSeatsAvailableException} is thrown
     *
     * @param seanceId the unique identifier of the seance to book
     * @param userId   the unique identifier of the user making the reservation
     * @param quantity the number of seats being reserved
     * @return the newly created and saved {@link Reservation} entity
     * @throws java.util.NoSuchElementException if either the seance or the user is not found in the database
     * @throws NoSeatsAvailableException if the requested quantity is greater than the available seats
     */
    @Transactional
    public Reservation createReservation(Long seanceId, Long userId, int quantity){
        Seance seance = seanceRepository.findByIdForUpdate(seanceId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();

        if (seance.getAvailableSeats() < quantity){
            throw new NoSeatsAvailableException("Seulement " + seance.getAvailableSeats() + " places disponibles");
        }

        List<Reservation> existingList = reservationRepository.findAllByUserIdAndSeanceId(userId, seanceId);


        seance.setAvailableSeats(seance.getAvailableSeats() - quantity);
        seanceRepository.save(seance);

        if (!existingList.isEmpty()){
            Reservation resa =  existingList.get(0);
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

        return  reservationRepository.save(reservation);
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
