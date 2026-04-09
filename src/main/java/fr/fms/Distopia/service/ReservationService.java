package fr.fms.Distopia.service;

import fr.fms.Distopia.dao.ReservationRepository;
import fr.fms.Distopia.dao.SeanceRepository;
import fr.fms.Distopia.dao.UserRepository;
import fr.fms.Distopia.entities.Reservation;
import fr.fms.Distopia.entities.Seance;
import fr.fms.Distopia.entities.User;
import fr.fms.Distopia.exceptions.NoSeatsAvailableException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationService {
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SeanceRepository seanceRepository;

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
        seance.setAvailableSeats(seance.getAvailableSeats() - quantity);
        seanceRepository.save(seance);

        Reservation reservation = new Reservation();
        reservation.setReservedAt(LocalDateTime.now());
        reservation.setSeance(seance);
        reservation.setUser(user);
        reservation.setQuantity(quantity);

        return  reservationRepository.save(reservation);
    }
}
