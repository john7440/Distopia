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
    public List<Reservation> getByUser(Long userId){
        return reservationRepository.findByUserIdOrderByReservedAtDesc(userId);
    }

    //------------------créer une réservation---------------
    @Transactional
    public Reservation createReservation(Long seanceId, Long userId){
        Seance seance = seanceRepository.findById(seanceId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();

        if (seance.getAvailableSeats() <= 0){
            throw new NoSeatsAvailableException("Plus de places disponibles pour cetté séance");
        }
        seance.setAvailableSeats(seance.getAvailableSeats() - 1);
        seanceRepository.save(seance);

        Reservation reservation = new Reservation();
        reservation.setReservedAt(LocalDateTime.now());
        reservation.setSeance(seance);
        reservation.setUser(user);

        return  reservationRepository.save(reservation);
    }
}
