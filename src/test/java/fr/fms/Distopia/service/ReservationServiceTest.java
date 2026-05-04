package fr.fms.Distopia.service;

import fr.fms.Distopia.dao.ReservationRepository;
import fr.fms.Distopia.dao.SeanceRepository;
import fr.fms.Distopia.dao.UserRepository;
import fr.fms.Distopia.entities.Movie;
import fr.fms.Distopia.entities.Reservation;
import fr.fms.Distopia.entities.Seance;
import fr.fms.Distopia.entities.User;
import fr.fms.Distopia.exceptions.NoSeatsAvailableException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock private SeanceRepository seanceRepository;
    @Mock private ReservationRepository reservationRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private ReservationService reservationService;

    //------------test getByUser()----------------
    @Test
    @DisplayName("getByUser() - should calls findByUserIdOrderByReservedAtDesc()")
    void getByUser_ShouldCallFindByUserIdOrderByReservedAtDesc() {
        reservationService.getByUser(1L);

        verify(reservationRepository).findByUserIdOrderByReservedAtDesc(1L);
    }

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

    //Pattern de test :
    // GIVEN — on prépare les données fictives
    // WHEN — on appelle la méthode à tester
    // THEN — on vérifie le résultat

    //------Test 1---------Réservation normale avec places dispos-------------
    @Test
    void should_create_reservation_when_seats_are_available() {
        //GIVEN
        Seance seance = buildSeance(10);
        User user = buildUser();

        when(seanceRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(seance));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(reservationRepository.findAllByUserIdAndSeanceId(2L, 1L)).thenReturn(List.of());
        when(reservationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        //WHEN
        Reservation result = reservationService.createReservation(1L,2L,3);

        //THEN
        assertThat(result.getQuantity()).isEqualTo(3);
        assertThat(seance.getAvailableSeats()).isEqualTo(7); //10-3
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    //-----Test2--------Plus assez de places---------------------------
    @Test
    void should_throw_exception_when_not_enough_seats_available() {
        //GIVEN
        Seance seance = buildSeance(2);

        when(seanceRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(seance));
        when(userRepository.findById(2L)).thenReturn(Optional.of(buildUser()));

        // WHEN + THEN
        assertThatThrownBy(() -> reservationService.createReservation(1L,2L,5))
        .isInstanceOf(NoSeatsAvailableException.class).hasMessageContaining("2");

        verify(reservationRepository, never()).save(any());
    }

    //-----Test 3---------Séance complète (0 places restantes)
    @Test
    void should_throw_exception_when_seance_is_full(){
        //GIVEN
        Seance seance = buildSeance(0);

        when(seanceRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(seance));
        when(userRepository.findById(2L)).thenReturn(Optional.of(buildUser()));

        //WHEN + THEN
        assertThatThrownBy(() -> reservationService.createReservation(1L,2L,1))
                .isInstanceOf(NoSeatsAvailableException.class);
    }

    //----Test 4-------- fusion si réservation déjà éxistante-----------
    @Test
    void should_update_existing_reservation_instead_of_creating_new(){
        //GIVEN
        Seance seance = buildSeance(10);
        User user = buildUser();

        Reservation existing = new Reservation();
        existing.setId(99L);
        existing.setQuantity(2);
        existing.setUser(user);
        existing.setSeance(seance);

        when(seanceRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(seance));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(reservationRepository.findAllByUserIdAndSeanceId(2L, 1L)).thenReturn(List.of(existing));
        when(reservationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        //WHEN
        Reservation result = reservationService.createReservation(1L,2L,3);

        //THEN
        assertThat(result.getQuantity()).isEqualTo(5);
        assertThat(result.getId()).isEqualTo(99L);
        assertThat(seance.getAvailableSeats()).isEqualTo(7);
    }

    //----Test 5 ------------------une réservation avec exactement 1 place restante-------------------
    @Test
    void should_allow_reservation_when_exactly_one_seat_left(){
        //GIVEN
        Seance seance = buildSeance(1);

        when(seanceRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(seance));
        when(userRepository.findById(2L)).thenReturn(Optional.of(buildUser()));
        when(reservationRepository.findAllByUserIdAndSeanceId(2L, 1L)).thenReturn(List.of());
        when(reservationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        //WHEN
        Reservation result = reservationService.createReservation(1L,2L,1);

        //THEN
        assertThat(result.getQuantity()).isEqualTo(1);
        assertThat(seance.getAvailableSeats()).isZero();
    }

    //----------------------test réservation en trop supprimées----------------
    @Test
    @DisplayName("createReservation() - merges and delete duplicate booking")
    void createReservation_ShouldMergeAndDeleteDuplicatedBooking() {
        Seance seance = new Seance();
        seance.setAvailableSeats(10);

        Reservation r1 = new Reservation();
        r1.setQuantity(2);

        Reservation r2 = new Reservation();
        r2.setQuantity(3);

        List<Reservation> existingList =  new ArrayList<>(List.of(r1,r2));

        when(seanceRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(seance));
        when(userRepository.findById(1L)).thenReturn(Optional.of(buildUser()));
        when(reservationRepository.findAllByUserIdAndSeanceId(1L, 1L)).thenReturn(existingList);

        reservationService.createReservation(1L,1L,2);

        verify(reservationRepository).deleteAll(existingList.subList(1, existingList.size()));
        assertThat(r1.getQuantity()).isEqualTo(4);
        verify(reservationRepository).save(r1);
    }

    //---------------------tests existsByUserAndSeance()--------------------
    @Test
    @DisplayName("existsByUserAndSeance() - return true if the booking exist")
    void existsByUserAndSeance_ShouldReturnTrueIfBookingExist() {
        when(reservationRepository.findByUserIdAndSeanceId(1L,1L))
                .thenReturn(Optional.of(new Reservation()));

        assertThat(reservationService.existsByUserAndSeance(1L,1L)).isTrue();
    }

    @Test
    @DisplayName("existsByUserAndSeance() - return false if the booking not exist")
    void existsByUserAndSeance_ShouldReturnFalseIfBookingNotExist() {
        when(reservationRepository.findByUserIdAndSeanceId(1L,1L))
                .thenReturn(Optional.empty());

        assertThat(reservationService.existsByUserAndSeance(1L,1L)).isFalse();
    }

    //--------------test getMovieIdBySeance()-------------------------------
    @Test
    @DisplayName("getMovieIdBySeance() - returns the correct movieId of the seance")
    void getMovieIdBySeance_ShouldReturnMovieIdBySeance() {
        Movie movie = new Movie();
        movie.setId(10L);

        Seance seance = new Seance();
        seance.setMovie(movie);

        when(seanceRepository.findById(1L)).thenReturn(Optional.of(seance));

        assertThat(reservationService.getMovieIdBySeance(1L)).isEqualTo(10L);
    }

    @Test
    @DisplayName("getMovieIdBySeance() - returns null if empty")
    void getMovieIdBySeance_ShouldReturnNullIfEmpty() {
        when(seanceRepository.findById(1L)).thenReturn(Optional.empty());

        assertThat(reservationService.getMovieIdBySeance(1L)).isNull();
    }
}
