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
import org.mockito.InOrder;
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

        verify(seanceRepository).findUpcomingActiveByMovieAndCinema(eq(1L),eq(1L), any(LocalDateTime.class));
    }

    //-------------test findById()-------------------
    @Test
    @DisplayName("findById() - should call findById() Repo")
    void getByMovieAndCinema_ShouldCallsFindByIdRepo() {
        seanceService.findById(1L);

        verify(seanceRepository).findById(1L);
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
        when(seanceRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(seance));
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
    @DisplayName("save() - rejects unknown cinema")
    void save_shouldThrowRuntimeExceptionWhenCinemaIdNotFound() {
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(cinemaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seanceService.save(null, futureDate, 200, 15.0, 1L, 99L))
               .isInstanceOf(RuntimeException.class).hasMessage("Cinéma introuvable");

        verify(seanceRepository, never()).save(any(Seance.class));
    }

    @Test
    @DisplayName("save() - rejects new screening without a movie")
    void save_ShouldRejectCreatingScreeningWithoutMovie() {
        assertThatThrownBy(() -> seanceService.save(null, futureDate, 200, 15.0, null, 1L
        )).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("film");

        verify(seanceRepository, never()).save(any(Seance.class));
        verifyNoInteractions(movieRepository, cinemaRepository);
        verify(seanceRepository, never()).save(any(Seance.class));
    }


    @Test
    @DisplayName("save() - refuses seat changes when reservations exist")
    void save_shouldRejectSeatChangeWhenReservationsExist() {
        when(seanceRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(seance));
        when(reservationRepository.countBySeanceIds(List.of(1L))).thenReturn(1L);

        assertThatThrownBy(() ->
                seanceService.save(1L, futureDate, 200, 12.0, 1L, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("places");

        assertThat(seance.getAvailableSeats()).isEqualTo(100);
        verify(seanceRepository, never()).save(any(Seance.class));
    }

    @Test
    @DisplayName("save() - allows price change without changing reserved seats")
    void save_shouldAllowPriceChangeWhenSeatsStayUnchanged() {
        when(seanceRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(seance));
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(cinemaRepository.findById(1L)).thenReturn(Optional.of(cinema));
        when(seanceRepository.save(any(Seance.class))).thenAnswer(inv -> inv.getArgument(0));

        Seance result = seanceService.save(
                1L, futureDate, 100, 15.0, 1L, 1L);

        assertThat(result.getAvailableSeats()).isEqualTo(100);
        assertThat(result.getPrice()).isEqualTo(15.0);
    }


    //---------------------test de la méthode delete()--------------------
    @Test
    @DisplayName("delete() - deletes screening without reservations")
    void delete_shouldDeleteSeanceWhenItHasNoReservations() {
        when(seanceRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(seance));
        when(reservationRepository.countBySeanceIds(List.of(1L))).thenReturn(0L);

        seanceService.delete(1L);

        verify(reservationRepository).countBySeanceIds(List.of(1L));
        verify(seanceRepository).delete(seance);
    }

    @Test
    @DisplayName("delete() - refuses deletion when reservations exist")
    void delete_shouldRefuseDeletionWhenSeanceHasReservations() {
        when(seanceRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(seance));

        when(reservationRepository.countBySeanceIds(List.of(1L))).thenReturn(1L);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> seanceService.delete(1L));

        assertEquals("Impossible de supprimer une séance avec des réservations", exception.getMessage());
        verify(seanceRepository, never()).delete(any(Seance.class));
    }


    /**
     * Verifies that deleting an individual screening
     * acquires a pessimistic lock before checking
     * whether reservations prevent deletion.
     *
     * <p>The screening must only be deleted after
     * the reservation check has succeeded.</p>
     */
    @Test
    @DisplayName("delete() - locks screening before checking reservations")
    void delete_ShouldLockSeanceBeforeCheckingReservations() {
        when(seanceRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(seance));

        seanceService.delete(1L);

        InOrder order = inOrder(seanceRepository, reservationRepository);

        order.verify(seanceRepository).findByIdForUpdate(1L);
        order.verify(reservationRepository).countBySeanceIds(List.of(1L));
        order.verify(seanceRepository).delete(seance);
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
        stubLockedSeances(1L, 2L, 3L);

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
        stubLockedSeances(1L, 2L, 3L);

        when(reservationRepository.countBySeanceIds(ids)).thenReturn(1L);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> seanceService.deleteSelected(ids));

        assertEquals("Impossible de supprimer une ou plusieurs séances car elles possèdent déjà des réservations",
                exception.getMessage());
        verify(reservationRepository).countBySeanceIds(ids);
        verify(seanceRepository, never()).deleteAllByIdInBatch(anyList());
    }

    @Test
    @DisplayName("deleteSelected() - locks screenings before checking reservations")
    void deleteSelected_ShouldLockSeancesBeforeCheckingReservations() {
        List<Long> ids = List.of(2L, 1L);

        stubLockedSeances(1L, 2L);

        int deleted = seanceService.deleteSelected(ids);

        assertThat(deleted).isEqualTo(2);

        InOrder order = inOrder(seanceRepository, reservationRepository);

        order.verify(seanceRepository).findByIdForUpdate(1L);
        order.verify(seanceRepository).findByIdForUpdate(2L);
        order.verify(reservationRepository).countBySeanceIds(ids);
        order.verify(seanceRepository).deleteAllByIdInBatch(ids);
    }

    //--------------test for deleteByAdminFilters----------------------------

    @Test
    @DisplayName("deleteByAdminFilters - Should return zero when no seance matches filters")
    void deleteByAdminFilters_shouldReturnZeroWhenNoSeanceMatchesFilters() {
        when(seanceRepository.findIdsByAdminFilters("avatar", 5L, true)).thenReturn(List.of());

        int deleted = seanceService.deleteByAdminFilters("avatar", 5L, true);

        assertEquals(0, deleted);
        verify(seanceRepository).findIdsByAdminFilters("avatar", 5L, true);
        verifyNoInteractions(reservationRepository);
        verify(seanceRepository, never()).deleteAllByIdInBatch(anyList());
    }

    @Test
    @DisplayName("deleteByAdminFilters - Should delete matching seances when none has reservations")
    void deleteByAdminFilters_shouldDeleteMatchingSeancesWhenNoneHasReservations() {
        List<Long> ids = List.of(1L, 2L);
        stubLockedSeances(1L, 2L);

        when(seanceRepository.findIdsByAdminFilters("avatar", 5L, true)).thenReturn(ids);
        when(reservationRepository.countBySeanceIds(ids)).thenReturn(0L);

        int deleted = seanceService.deleteByAdminFilters("avatar", 5L, true);

        assertEquals(2, deleted);
        verify(seanceRepository).findIdsByAdminFilters("avatar", 5L, true);
        verify(reservationRepository).countBySeanceIds(ids);
        verify(seanceRepository).deleteAllByIdInBatch(ids);
    }

    @Test
    @DisplayName("deleteByAdminFilters - Should refuse deletion when one matching seance has reservations")
    void deleteByAdminFilters_shouldRefuseDeletionWhenOneMatchingSeanceHasReservations() {
        List<Long> ids = List.of(1L, 2L);
        stubLockedSeances(1L, 2L);

        when(seanceRepository.findIdsByAdminFilters("avatar", 5L, true)).thenReturn(ids);
        when(reservationRepository.countBySeanceIds(ids)).thenReturn(1L);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> seanceService.deleteByAdminFilters("avatar", 5L, true));

        assertEquals("Impossible de supprimer une ou plusieurs séances car elles possèdent déjà des réservations", exception.getMessage());
        verify(seanceRepository).findIdsByAdminFilters("avatar", 5L, true);
        verify(reservationRepository).countBySeanceIds(ids);
        verify(seanceRepository, never()).deleteAllByIdInBatch(anyList());
    }

    @Test
    @DisplayName("deleteByAdminFilters() - locks screenings before checking reservations")
    void deleteByAdminFilters_ShouldLockSeancesBeforeCheckingReservations() {
        List<Long> ids = List.of(2L, 1L);

        when(seanceRepository.findIdsByAdminFilters("avatar", 5L, true)).thenReturn(ids);

        stubLockedSeances(1L, 2L);

        int deleted = seanceService.deleteByAdminFilters("avatar", 5L, true);

        assertThat(deleted).isEqualTo(2);

        InOrder order = inOrder(seanceRepository, reservationRepository);

        order.verify(seanceRepository).findIdsByAdminFilters("avatar", 5L, true);
        order.verify(seanceRepository).findByIdForUpdate(1L);
        order.verify(seanceRepository).findByIdForUpdate(2L);
        order.verify(reservationRepository).countBySeanceIds(ids);
        order.verify(seanceRepository).deleteAllByIdInBatch(ids);
    }

    //------------------tests for searchAdmin() ---------------------------
    @Test
    @DisplayName("searchAdmin() - returns paged seances using keyword and cinema filters")
    void searchAdmin_ShouldReturnPagedSeancesUsingKeywordAndCinemaFilters() {
        Page<Seance> page = new PageImpl<>(List.of(seance));

        when(seanceRepository.searchAdmin(eq("Inception"), eq(1L),anyBoolean(),any(Pageable.class))).thenReturn(page);

        Page<Seance> result = seanceService.searchAdmin("Inception", 1L,true,"sortField","sortDir",0);

        assertThat(result.getContent()).containsExactly(seance);
        verify(seanceRepository).searchAdmin(eq("Inception"), eq(1L),anyBoolean(),any(Pageable.class));

    }

    @Test
    @DisplayName("searchAdmin() -  returns empty page when no seances match")
    void searchAdmin_ShouldReturnEmptyPageWhenNoSeancesMatch() {
        Page<Seance> emptyPage = new PageImpl<>(List.of());

        when(seanceRepository.searchAdmin(eq("Inconnu"), eq(99L),anyBoolean(),any(Pageable.class))).thenReturn(emptyPage);

        Page<Seance> result = seanceService.searchAdmin("Inconnu", 99L,true,"sortField","sortDir",0);

        assertThat(result.getContent()).isEmpty();
        verify(seanceRepository).searchAdmin(eq("Inconnu"), eq(99L),anyBoolean(),any(Pageable.class));
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


    @Test
    @DisplayName("sync - archives past seances without automatic reactivation")
    void sync_shouldArchivePastWithoutReactivatingFuture() {
        seanceService.syncSeanceActivityStatus();

        verify(seanceRepository).archivePastSeances(any(LocalDateTime.class));

        verifyNoMoreInteractions(seanceRepository);
    }


    @Test
    @DisplayName("save - refuses reactivation in deleted cinema ")
    void save_shouldRejectReactivationInDeletedCinema() {
        seance.setActive(false);
        cinema.setDeleted(true);

        when(seanceRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(seance));
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(cinemaRepository.findById(1L)).thenReturn(Optional.of(cinema));

        assertThatThrownBy(() ->
                seanceService.save(1L, futureDate, 100, 12.0, 1L, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cinéma");

        verify(seanceRepository, never()).save(any(Seance.class));
    }


    @Test
    @DisplayName("save - explicitly reschedules archived seance")
    void save_shouldReactivateWhenRescheduledToFuture() {
        seance.setDateTime(LocalDateTime.now().minusDays(1));
        seance.setActive(false);

        when(seanceRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(seance));
        when(movieRepository.findById(1L))
                .thenReturn(Optional.of(movie));
        when(cinemaRepository.findById(1L))
                .thenReturn(Optional.of(cinema));
        when(seanceRepository.save(any(Seance.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Seance result = seanceService.save(
                1L, futureDate, 100, 12.0, 1L, 1L);

        assertThat(result.isActive()).isTrue();
        assertThat(result.getDateTime()).isEqualTo(futureDate);
    }


    @Test
    @DisplayName("save - preserves inactive future seance")
    void save_shouldKeepFutureInactiveSeanceInactive() {
        seance.setActive(false);

        when(seanceRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(seance));
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(cinemaRepository.findById(1L)).thenReturn(Optional.of(cinema));
        when(seanceRepository.save(any(Seance.class))).thenAnswer(inv -> inv.getArgument(0));

        Seance result = seanceService.save(1L, futureDate, 100, 15.0, 1L, 1L);

        assertThat(result.isActive()).isFalse();
        assertThat(result.getPrice()).isEqualTo(15.0);
    }


    @Test
    @DisplayName("save - refuses seance for deleted movie")
    void save_shouldRejectDeletedMovie() {
        movie.setDeleted(true);

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));

        assertThatThrownBy(() ->
                seanceService.save(null, futureDate, 100, 12.0, 1L, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("film supprimé");

        verify(seanceRepository, never()).save(any(Seance.class));
    }


    @Test
    @DisplayName("save - rejects price change when reservations exist")
    void save_shouldRejectPriceChangeWhenReservationsExist() {
        when(seanceRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(seance));
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(cinemaRepository.findById(1L)).thenReturn(Optional.of(cinema));
        when(reservationRepository.countBySeanceIds(List.of(1L))).thenReturn(1L);

        assertThatThrownBy(() ->
                seanceService.save(1L, futureDate, 100, 15.0, 1L, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("réservations");

        verify(seanceRepository, never()).save(any(Seance.class));
    }

    @Test
    @DisplayName("save - rejects date change when reservations exist")
    void save_shouldRejectDateChangeWhenReservationsExist() {
        when(seanceRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(seance));
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(cinemaRepository.findById(1L)).thenReturn(Optional.of(cinema));
        when(reservationRepository.countBySeanceIds(List.of(1L))).thenReturn(1L);

        assertThatThrownBy(() ->
                seanceService.save(1L, futureDate.plusDays(1), 100, 12.0, 1L, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("réservations");

        verify(seanceRepository, never()).save(any(Seance.class));
    }


    @Test
    @DisplayName("save - rejects movie change when reservations exist")
    void save_shouldRejectMovieChangeWhenReservationsExist() {
        Movie otherMovie = new Movie();
        otherMovie.setId(2L);
        otherMovie.setTitle("Other Movie");

        when(seanceRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(seance));
        when(movieRepository.findById(2L)).thenReturn(Optional.of(otherMovie));
        when(cinemaRepository.findById(1L)).thenReturn(Optional.of(cinema));
        when(reservationRepository.countBySeanceIds(List.of(1L))).thenReturn(1L);

        assertThatThrownBy(() -> seanceService.save(1L, futureDate, 100, 12.0, 2L, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("réservations");

        assertThat(seance.getMovie()).isSameAs(movie);
        verify(seanceRepository, never()).save(any(Seance.class));
    }


    @Test
    @DisplayName("save - rejects cinema change when reservations exist")
    void save_shouldRejectCinemaChangeWhenReservationsExist() {
        Cinema otherCinema = new Cinema();
        otherCinema.setId(2L);
        otherCinema.setName("Other Cinema");

        when(seanceRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(seance));
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(cinemaRepository.findById(2L)).thenReturn(Optional.of(otherCinema));
        when(reservationRepository.countBySeanceIds(List.of(1L))).thenReturn(1L);

        assertThatThrownBy(() -> seanceService.save(1L, futureDate, 100, 12.0, 1L, 2L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("réservations");

        assertThat(seance.getCinema()).isSameAs(cinema);
        verify(seanceRepository, never()).save(any(Seance.class));
    }


    /**
     * Stubs pessimistically locked screenings for service tests
     *
     * @param ids the identifiers of existing screenings
     */
    private void stubLockedSeances(Long... ids) {
        for (Long id : ids) {
            Seance lockedSeance = new Seance();
            lockedSeance.setId(id);

            when(seanceRepository.findByIdForUpdate(id)).thenReturn(Optional.of(lockedSeance));
        }
    }
}
