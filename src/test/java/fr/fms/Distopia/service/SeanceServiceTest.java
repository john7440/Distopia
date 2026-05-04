package fr.fms.Distopia.service;

import fr.fms.Distopia.dao.CinemaRepository;
import fr.fms.Distopia.dao.MovieRepository;
import fr.fms.Distopia.dao.SeanceRepository;
import fr.fms.Distopia.entities.Cinema;
import fr.fms.Distopia.entities.Movie;
import fr.fms.Distopia.entities.Reservation;
import fr.fms.Distopia.entities.Seance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
 class SeanceServiceTest {
    @Mock
    private SeanceRepository seanceRepository;
    @Mock
    private MovieRepository movieRepository;
    @Mock
    private CinemaRepository cinemaRepository;
    @InjectMocks
    private SeanceService seanceService;

    private Seance seance;
    private Movie movie;
    private Cinema cinema;
    private final LocalDateTime futureDate = LocalDateTime.now().plusDays(7);

    @BeforeEach
    void setUp() {
        cinema = new Cinema();
        cinema.setId(1L);
        cinema.setName("Cinema Test");

        movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Movie Test");

        seance = new Seance();
        seance.setId(1L);
        seance.setDateTime(futureDate);
        seance.setAvailableSeats(100);
        seance.setPrice(12.0);
        seance.setMovie(movie);
        seance.setCinema(cinema);
        seance.setReservations(new ArrayList<>());
    }

    //--------------------test getByMovieAndCinema() ------------------------------
    @Test
    @DisplayName("getByMovieAndCinema() - should call findByMovieIdAndCinemaIdOrderByDateTimeAsc Repo")
    void getByMovieAndCinema_ShouldCallsTheCorrectRepo() {
        seanceService.getByMovieAndCinema(1L,1L);

        verify(seanceRepository).findByMovieIdAndCinemaIdOrderByDateTimeAsc(1L,1L);
    }

    //-------------test findById()-------------------
    @Test
    @DisplayName("findById() - should call findById() Repo")
    void getByMovieAndCinema_ShouldCallsFindByIdRepo() {
        seanceService.findById(1L);

        verify(seanceRepository).findById(1L);
    }

    //-------------test getAll()-------------------
    @Test
    @DisplayName("getAll() - should call findAll() Repository")
    void getByMovieAndCinema_ShouldCallsFindAllRepository() {
        seanceService.getAll();

        verify(seanceRepository).findAll();
    }

    //-------------test getUpcomingByMovie()-------------------
    @Test
    @DisplayName("getUpcomingByMovie() - should call findByMovieAndDateTimeAfterOrderByDateTimeAsc Repo")
    void getUpcomingByMovie_ShouldCallsTheCorrectRepo() {
        seanceService.getUpcomingByMovie(1L);

        verify(seanceRepository).findByMovieIdAndDateTimeAfterOrderByDateTimeAsc(eq(1L),any(LocalDateTime.class));
    }

    //------------------tests du save()-----------------------
    @Test
    @DisplayName("save() - creates a new seance when id is null")
    void save_shouldCreateNewSeanceWhenIdIsNull() {
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(cinemaRepository.findById(1L)).thenReturn(Optional.of(cinema));
        when(seanceRepository.save(any(Seance.class))).thenAnswer(i -> i.getArgument(0));

        Seance result = seanceService.save(null, futureDate, 100, 10.0,1L,1L);

        assertThat(result.getAvailableSeats()).isEqualTo(100);
        assertThat(result.getPrice()).isEqualTo(10.0);
        assertThat(result.getCinema()).isEqualTo(cinema);
        assertThat(result.getMovie()).isEqualTo(movie);
        verify(seanceRepository).save(any(Seance.class));
    }

    @Test
    @DisplayName("save() - updates existing seance when id is found")
    void save_shouldUpdateExistingSeanceWhenIdIsFound() {
        when(seanceRepository.findById(1L)).thenReturn(Optional.of(seance));
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(cinemaRepository.findById(1L)).thenReturn(Optional.of(cinema));
        when(seanceRepository.save(any(Seance.class))).thenAnswer(i -> i.getArgument(0));

        Seance result = seanceService.save(1L, futureDate, 200, 15.0,1L,1L);

        assertThat(result.getAvailableSeats()).isEqualTo(200);
        assertThat(result.getPrice()).isEqualTo(15.0);
        verify(seanceRepository).save(seance);
    }

    @Test
    @DisplayName("save() - throws NoSuchElementException when movieId not found")
    void save_shouldThrowNoSuchElementExceptionWhenMovieIdNotFound() {
        when(movieRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seanceService.save(null, futureDate, 200, 15.0,99L,1L))
            .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("save() - throws RuntimeException when cinemaId not found")
    void save_shouldThrowRuntimeExceptionWhenCinemaIdNotFound() {
        when(cinemaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seanceService.save(null, futureDate, 200, 15.0,null,99L))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Cinéma introuvable");
    }

    @Test
    @DisplayName("save() - skips movie assignment when movieId is null")
    void save_shouldSkipMovieAssignmentWhenMovieIdIsNull() {
        when(cinemaRepository.findById(1L)).thenReturn(Optional.of(cinema));
        when(seanceRepository.save(any(Seance.class))).thenAnswer(i -> i.getArgument(0));

        Seance result = seanceService.save(null, futureDate, 200, 15.0,null,1L);

        assertThat(result.getMovie()).isNull();
        verify(movieRepository, never()).findById(any());
    }

    //---------------------test de la méthode delete()--------------------
    @Test
    @DisplayName("delete() - deletes seance successfully when no reservations exists")
    void delete_shouldDeleteSeanceSuccessfullyWhenNoReservationsExists() {
        when(seanceRepository.findById(1L)).thenReturn(Optional.of(seance));

        seanceService.delete(1L);

        verify(seanceRepository).delete(seance);
    }

    @Test
    @DisplayName("delete() - throws IllegalStateException when seance has reservations")
    void delete_shouldThrowIllegalStateExceptionWhenSeanceHasReservations() {
        Reservation reservation = new Reservation();
        seance.getReservations().add(reservation);
        when(seanceRepository.findById(1L)).thenReturn(Optional.of(seance));

        assertThatThrownBy(() -> seanceService.delete(1L)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("réservations");
    }

    @Test
    @DisplayName("delete() - throws NoSuchElementException when seance id not found")
    void delete_shouldThrowNoSuchElementExceptionWhenSeanceIdNotFound() {
        when(seanceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seanceService.delete(99L)).isInstanceOf(NoSuchElementException.class);
    }
}
