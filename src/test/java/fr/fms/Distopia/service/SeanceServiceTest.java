package fr.fms.Distopia.service;

import fr.fms.Distopia.dao.CinemaRepository;
import fr.fms.Distopia.dao.MovieRepository;
import fr.fms.Distopia.dao.ReservationRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    @Mock
    private ReservationRepository reservationRepository;
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
    @DisplayName("delete - Should delete seance when it has no reservations")
    void delete_shouldDeleteSeanceWhenItHasNoReservations() {
        seance.setId(1L);
        seance.setReservations(new ArrayList<>());

        when(seanceRepository.findById(1L)).thenReturn(Optional.of(seance));

        seanceService.delete(1L);

        verify(seanceRepository).delete(seance);
    }

    @Test
    @DisplayName("delete - Should refuse deletion when seance has reservations")
    void delete_shouldRefuseDeletionWhenSeanceHasReservations() {
        seance.setId(1L);
        seance.setReservations(List.of(new Reservation()));

        when(seanceRepository.findById(1L)).thenReturn(Optional.of(seance));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class, () -> seanceService.delete(1L));

        assertEquals("Impossible de supprimer une séance avec des réservations", exception.getMessage());
        verify(seanceRepository, never()).delete(seance);
    }

    //-----------------tests for deleteSelected() -------------------
    @Test
    @DisplayName("deleteSelected - Should return zero when no seance is selected")
    void deleteSelected_shouldReturnZeroWhenNoSeanceIsSelected() {
        int deleted = seanceService.deleteSelected(null);

        assertEquals(0, deleted);
        verifyNoInteractions(reservationRepository);
        verify(seanceRepository, never()).deleteAllByIdInBatch(anyList());
    }

    @Test
    @DisplayName("deleteSelected - Should return zero when selected list is empty")
    void deleteSelected_shouldReturnZeroWhenSelectedListIsEmpty() {
        int deleted = seanceService.deleteSelected(List.of());

        assertEquals(0, deleted);
        verifyNoInteractions(reservationRepository);
        verify(seanceRepository, never()).deleteAllByIdInBatch(anyList());
    }

    @Test
    @DisplayName("deleteSelected - Should delete selected seances when none has reservations")
    void deleteSelected_shouldDeleteSelectedSeancesWhenNoneHasReservations() {
        List<Long> ids = List.of(1L, 2L, 3L);

        when(reservationRepository.countBySeanceIds(ids)).thenReturn(0L);

        int deleted = seanceService.deleteSelected(ids);

        assertEquals(3, deleted);
        verify(reservationRepository).countBySeanceIds(ids);
        verify(seanceRepository).deleteAllByIdInBatch(ids);
    }

    @Test
    @DisplayName("deleteSelected - Should refuse deletion when one selected seance has reservations")
    void deleteSelected_shouldRefuseDeletionWhenOneSelectedSeanceHasReservations() {
        List<Long> ids = List.of(1L, 2L, 3L);

        when(reservationRepository.countBySeanceIds(ids)).thenReturn(1L);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> seanceService.deleteSelected(ids));

        assertEquals("Impossible de supprimer une ou plusieurs séances car elles possèdent déjà des réservations",
                exception.getMessage());
        verify(reservationRepository).countBySeanceIds(ids);
        verify(seanceRepository, never()).deleteAllByIdInBatch(anyList());
    }

    //--------------test for deleteByAdminFilters----------------------------

    @Test
    @DisplayName("deleteByAdminFilters - Should return zero when no seance matches filters")
    void deleteByAdminFilters_shouldReturnZeroWhenNoSeanceMatchesFilters() {
        when(seanceRepository.findIdsByAdminFilters("avatar", 5L)).thenReturn(List.of());

        int deleted = seanceService.deleteByAdminFilters("avatar", 5L);

        assertEquals(0, deleted);
        verify(seanceRepository).findIdsByAdminFilters("avatar", 5L);
        verifyNoInteractions(reservationRepository);
        verify(seanceRepository, never()).deleteAllByIdInBatch(anyList());
    }

    @Test
    @DisplayName("deleteByAdminFilters - Should delete matching seances when none has reservations")
    void deleteByAdminFilters_shouldDeleteMatchingSeancesWhenNoneHasReservations() {
        List<Long> ids = List.of(1L, 2L);

        when(seanceRepository.findIdsByAdminFilters("avatar", 5L)).thenReturn(ids);
        when(reservationRepository.countBySeanceIds(ids)).thenReturn(0L);

        int deleted = seanceService.deleteByAdminFilters("avatar", 5L);

        assertEquals(2, deleted);
        verify(seanceRepository).findIdsByAdminFilters("avatar", 5L);
        verify(reservationRepository).countBySeanceIds(ids);
        verify(seanceRepository).deleteAllByIdInBatch(ids);
    }

    @Test
    @DisplayName("deleteByAdminFilters - Should refuse deletion when one matching seance has reservations")
    void deleteByAdminFilters_shouldRefuseDeletionWhenOneMatchingSeanceHasReservations() {
        List<Long> ids = List.of(1L, 2L);

        when(seanceRepository.findIdsByAdminFilters("avatar", 5L)).thenReturn(ids);
        when(reservationRepository.countBySeanceIds(ids)).thenReturn(1L);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> seanceService.deleteByAdminFilters("avatar", 5L));

        assertEquals("Impossible de supprimer une ou plusieurs séances car elles possèdent déjà des réservations", exception.getMessage());
        verify(seanceRepository).findIdsByAdminFilters("avatar", 5L);
        verify(reservationRepository).countBySeanceIds(ids);
        verify(seanceRepository, never()).deleteAllByIdInBatch(anyList());
    }

    //------------------tests for searchAdmin() ---------------------------
    @Test
    @DisplayName("searchAdmin() - returns paged seances using keyword and cinema filters")
    void searchAdmin_ShouldReturnPagedSeancesUsingKeywordAndCinemaFilters() {
        Page<Seance> page = new PageImpl<>(List.of(seance));

        when(seanceRepository.searchAdmin(eq("Inception"), eq(1L),any(Pageable.class))).thenReturn(page);

        Page<Seance> result = seanceService.searchAdmin("Inception", 1L,"sortField","sortDir",0);

        assertThat(result.getContent()).containsExactly(seance);
        verify(seanceRepository).searchAdmin(eq("Inception"), eq(1L),any(Pageable.class));

    }

    @Test
    @DisplayName("searchAdmin() -  returns empty page when no seances match")
    void searchAdmin_ShouldReturnEmptyPageWhenNoSeancesMatch() {
        Page<Seance> emptyPage = new PageImpl<>(List.of());

        when(seanceRepository.searchAdmin(eq("Inconnu"), eq(99L),any(Pageable.class))).thenReturn(emptyPage);

        Page<Seance> result = seanceService.searchAdmin("Inconnu", 99L,"sortField","sortDir",0);

        assertThat(result.getContent()).isEmpty();
        verify(seanceRepository).searchAdmin(eq("Inconnu"), eq(99L),any(Pageable.class));
    }

    //------------------tests for getUpcomingSeances() ---------------------------
    @Test
    @DisplayName("getUpcomingSeances() - returns upcoming seances for movie")
    void getUpcomingSeances_ShouldReturnUpcomingSeancesForMovie() {

        Page<Seance> page = new PageImpl<>(List.of(seance));

        when(seanceRepository.findUpcomingSeancesByMovie(eq(1L), any(Pageable.class))).thenReturn(page);

        Page<Seance> result = seanceService.getUpcomingSeances(1L, 0, 10);

        assertThat(result.getContent()).containsExactly(seance);
        verify(seanceRepository).findUpcomingSeancesByMovie(eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("getUpcomingSeances() -  returns empty page when no upcoming seances exist")
    void getUpcomingSeances_ShouldReturnEmptyPageWhenNoUpcomingSeancesExist() {
        Page<Seance> emptyPage = new PageImpl<>(List.of());

        when(seanceRepository.findUpcomingSeancesByMovie(eq(1L), any(Pageable.class))).thenReturn(emptyPage);

        Page<Seance> result = seanceService.getUpcomingSeances(1L, 0, 10);

        assertThat(result.getContent()).isEmpty();
        verify(seanceRepository).findUpcomingSeancesByMovie(eq(1L),any(Pageable.class));
    }
}
