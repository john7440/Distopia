package fr.fms.Distopia.service;

import fr.fms.Distopia.dao.ReservationRepository;
import fr.fms.Distopia.entities.Reservation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReservationService {
    @Autowired
    private ReservationRepository reservationRepository;


    //--------reservations d'un utilistaure--------
    public List<Reservation> getByUser(Long userId){
        return reservationRepository.findByUserIdOrderByReservedAtDesc(userId);
    }

}
