package fr.fms.Distopia;

import fr.fms.Distopia.dao.ReservationRepository;
import fr.fms.Distopia.dao.SeanceRepository;
import fr.fms.Distopia.dao.UserRepository;
import fr.fms.Distopia.entities.Reservation;
import fr.fms.Distopia.entities.Seance;
import fr.fms.Distopia.entities.User;
import fr.fms.Distopia.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @Mock private SeanceRepository seanceRepository;
    @Mock private ReservationRepository reservationRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private ReservationService reservationService;

    //--------------------helpers avec données réutilisables----------------
    private Seance buildSeance(int availableSeats) {
        Seance seance = new Seance();
        seance.setId(1L);
        seance.setAvailableSeats(availableSeats);
        seance.setPrice(9.50);
        seance.setDateTime(LocalDateTime.now().plusDays(1));
        return seance;
    }

    private User buildUser() {
        User user = new User();
        user.setId(2L);
        user.setUsername("alice");
        return user;
    }

    //------Test 1---------Réservation normale avec places dispos-------------
    @Test
    void should_create_reservation_when_seats_are_available() {
        Seance seance = buildSeance(10);
        User user = buildUser();

        when(seanceRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(seance));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(reservationRepository.findAllByUserIdAndSeanceId(2L, 1L)).thenReturn(List.of());
        when(reservationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Reservation result = reservationService.createReservation(1L,2L,3);

        assertThat(result.getQuantity()).isEqualTo(3);
        assertThat(seance.getAvailableSeats()).isEqualTo(7); //10-3
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }
}
