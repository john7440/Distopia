package fr.fms.Distopia.integration.mariadb;

import fr.fms.Distopia.dao.CinemaRepository;
import fr.fms.Distopia.dao.MovieRepository;
import fr.fms.Distopia.dao.ReservationRepository;
import fr.fms.Distopia.dao.SeanceRepository;
import fr.fms.Distopia.dao.UserRepository;
import fr.fms.Distopia.entities.Cinema;
import fr.fms.Distopia.entities.Movie;
import fr.fms.Distopia.entities.Reservation;
import fr.fms.Distopia.entities.Role;
import fr.fms.Distopia.entities.Seance;
import fr.fms.Distopia.entities.User;
import fr.fms.Distopia.exceptions.NoSeatsAvailableException;
import fr.fms.Distopia.service.ReservationService;
import fr.fms.Distopia.service.SeanceService;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies reservation concurrency against a real MariaDB database.
 *
 * <p>Each booking runs in an independent transaction.
 * Pessimistic locking must prevent concurrent requests
 * from overselling available seats.</p>
 */
@DataJpaTest
@Import({ReservationService.class, SeanceService.class})
@ActiveProfiles("mariadb-test")
@TestPropertySource(locations = "classpath:application-mariadb-test.properties")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnabledIfSystemProperty(named = "distopia.mariadb.tests", matches = "true")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class MariaDbReservationConcurrencyIntegrationTest {

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private SeanceRepository seanceRepository;

    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private CinemaRepository cinemaRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private SeanceService seanceService;


    private enum DeletionMode {
        INDIVIDUAL,
        SELECTED,
        FILTERED
    }

    /**
     * Verifies that two concurrent users cannot book
     * the same last available seat.
     *
     * <p>The first transaction remains open while the
     * second booking is submitted. After the first
     * transaction commits, the second booking must fail.</p>
     */
    @Test
    @DisplayName("MariaDB - concurrent bookings cannot oversell the last seat")
    void reservations_ShouldPreventConcurrentOverselling() throws InterruptedException, ExecutionException, TimeoutException {
        Long seanceId = createScreeningWithOneSeat();
        Long firstUserId = createUser("d05a-first");
        Long secondUserId = createUser("d05a-second");

        CountDownLatch firstBooked = new CountDownLatch(1);
        CountDownLatch allowFirstCommit = new CountDownLatch(1);
        CountDownLatch secondStarted = new CountDownLatch(1);

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        boolean executorTerminated;

        try {
            Future<Long> firstBooking = executor.submit(() ->
                    transactionTemplate.execute(status -> {
                        Reservation reservation = reservationService.createReservation(seanceId, firstUserId, 1);
                        firstBooked.countDown();
                        awaitSignal(allowFirstCommit);

                        return reservation.getId();
                    })
            );

            awaitSignal(firstBooked);
            Future<Reservation> secondBooking = executor.submit(() -> {
                secondStarted.countDown();

                return reservationService.createReservation(seanceId, secondUserId, 1);
            });

            awaitSignal(secondStarted);

            assertThatThrownBy(() -> secondBooking.get(1, TimeUnit.SECONDS)).isInstanceOf(TimeoutException.class);

            allowFirstCommit.countDown();

            assertThat(firstBooking.get(20, TimeUnit.SECONDS)).isNotNull();

            assertThatThrownBy(() -> secondBooking.get(20, TimeUnit.SECONDS))
                    .isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(NoSeatsAvailableException.class);

            Seance persistedSeance = seanceRepository.findById(seanceId).orElseThrow();

            assertThat(persistedSeance.getAvailableSeats()).isZero();
            assertThat(reservationRepository.countBySeanceIds(List.of(seanceId))).isEqualTo(1L);

            assertThat(reservationRepository.findAllByUserIdAndSeanceId(firstUserId, seanceId))
                    .extracting(Reservation::getQuantity)
                    .containsExactly(1);

            assertThat(reservationRepository.findAllByUserIdAndSeanceId(secondUserId, seanceId)).isEmpty();


        } finally {
            allowFirstCommit.countDown();
            executor.shutdown();

            try {
                executorTerminated = executor.awaitTermination(20, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                executorTerminated = false;
            }

            if (!executorTerminated) {
                executor.shutdownNow();
            }
        }

        assertThat(executorTerminated).as("Concurrent booking tasks must terminate").isTrue();
    }


    /**
     * Verifies that concurrent administrative deletion cannot remove a screening with a committed reservation
     *
     * <p>All supported deletion modes must wait for the booking transaction and reject deletion once the reservation
     * becomes visible</p>
     *
     * @param mode the administrative deletion mode
     */
    @ParameterizedTest
    @EnumSource(DeletionMode.class)
    @DisplayName("MariaDB - concurrent deletion preserves booked screening")
    void deletion_ShouldPreserveConcurrentlyBookedScreening(DeletionMode mode)
            throws InterruptedException, ExecutionException, TimeoutException {

        Long seanceId = createScreeningWithOneSeat();

        Long cinemaId = seanceRepository.findById(seanceId)
                .orElseThrow()
                .getCinema()
                .getId();

        Long userId = createUser("d05b-" + mode.name());

        CountDownLatch bookingCompleted = new CountDownLatch(1);
        CountDownLatch allowBookingCommit = new CountDownLatch(1);
        CountDownLatch deletionStarted = new CountDownLatch(1);

        TransactionTemplate transactionTemplate =
                new TransactionTemplate(transactionManager);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        boolean executorTerminated;

        try {
            Future<Long> booking = executor.submit(() ->
                    transactionTemplate.execute(status -> {

                        Reservation reservation = reservationService.createReservation(seanceId, userId, 1);

                        bookingCompleted.countDown();
                        awaitSignal(allowBookingCommit);

                        return reservation.getId();
                    })
            );

            awaitSignal(bookingCompleted);

            Future<Void> deletion = executor.submit(() -> {
                deletionStarted.countDown();

                switch (mode) {
                    case INDIVIDUAL -> seanceService.delete(seanceId);
                    case SELECTED -> seanceService.deleteSelected(List.of(seanceId));
                    case FILTERED -> seanceService.deleteByAdminFilters(null, cinemaId, true);
                }
                return null;
            });

            awaitSignal(deletionStarted);

            assertThatThrownBy(() -> deletion.get(1, TimeUnit.SECONDS)).isInstanceOf(TimeoutException.class);

            allowBookingCommit.countDown();

            assertThat(booking.get(20, TimeUnit.SECONDS)).isNotNull();
            assertThatThrownBy(() -> deletion.get(20, TimeUnit.SECONDS)).isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("réservations");

            Seance persistedSeance = seanceRepository.findById(seanceId).orElseThrow();

            assertThat(persistedSeance.getAvailableSeats()).isZero();
            assertThat(reservationRepository.countBySeanceIds(List.of(seanceId))).isEqualTo(1L);
            assertThat(reservationRepository.findAllByUserIdAndSeanceId(userId, seanceId))
                    .extracting(Reservation::getQuantity)
                    .containsExactly(1);

        } finally {
            allowBookingCommit.countDown();
            executor.shutdown();

            try {
                executorTerminated = executor.awaitTermination(20, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                executorTerminated = false;
            }
            if (!executorTerminated) {
                executor.shutdownNow();
            }
        }

        assertThat(executorTerminated).as("Concurrent booking and deletion tasks must terminate").isTrue();
    }


    /**
     * Verifies that a concurrent reservation cannot book a screening deleted by another transaction
     *
     * <p>The deletion transaction acquires the lock first and remains open until the booking request starts.
     * Once deletion commits, the booking must be rejected without creating an orphaned reservation</p>
     *
     * @param mode the administrative deletion mode
     */
    @ParameterizedTest
    @EnumSource(DeletionMode.class)
    @DisplayName("MariaDB - concurrent booking cannot reserve deleted screening")
    void reservation_ShouldRejectConcurrentlyDeletedScreening(DeletionMode mode) throws InterruptedException, ExecutionException,
            TimeoutException {

        Long seanceId = createScreeningWithOneSeat();

        Long cinemaId = seanceRepository.findById(seanceId).orElseThrow().getCinema().getId();

        Long userId = createUser("d05f-" + mode.name());

        CountDownLatch deletionCompleted = new CountDownLatch(1);
        CountDownLatch allowDeletionCommit = new CountDownLatch(1);
        CountDownLatch bookingStarted = new CountDownLatch(1);

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        boolean executorTerminated;

        try {
            Future<?> deletion = executor.submit(() ->
                    transactionTemplate.executeWithoutResult(status -> {
                        deleteScreening(mode, seanceId, cinemaId);
                        seanceRepository.flush();
                        deletionCompleted.countDown();
                        awaitSignal(allowDeletionCommit);
                    })
            );

            awaitSignal(deletionCompleted);

            Future<Reservation> booking = executor.submit(() -> {
                bookingStarted.countDown();

                return reservationService.createReservation(seanceId, userId, 1);
            });

            awaitSignal(bookingStarted);

            assertThatThrownBy(() -> booking.get(1, TimeUnit.SECONDS)).isInstanceOf(TimeoutException.class);

            allowDeletionCommit.countDown();
            deletion.get(20, TimeUnit.SECONDS);

            assertThatThrownBy(() -> booking.get(20, TimeUnit.SECONDS)).isInstanceOf(ExecutionException.class)
                    .hasCauseInstanceOf(NoSuchElementException.class);

            assertThat(seanceRepository.findById(seanceId)).isEmpty();
            assertThat(reservationRepository.countBySeanceIds(List.of(seanceId))).isZero();
            assertThat(reservationRepository.findByUserIdOrderByReservedAtDesc(userId)).isEmpty();
            assertThat(userRepository.findById(userId)).isPresent();

        } finally {
            allowDeletionCommit.countDown();
            executor.shutdown();

            try {
                executorTerminated = executor.awaitTermination(20, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                executorTerminated = false;
            }
            if (!executorTerminated) {
                executor.shutdownNow();
            }
        }

        assertThat(executorTerminated).as("Concurrent deletion and booking tasks must terminate").isTrue();
    }



    /**
     * Creates a future active screening with one available seat.
     *
     * @return the persisted screening identifier
     */
    private Long createScreeningWithOneSeat() {
        Movie movie = new Movie();
        movie.setTitle("Concurrency test movie");
        movie = movieRepository.saveAndFlush(movie);

        Cinema cinema = new Cinema();
        cinema.setName("Concurrency test cinema");
        cinema.setAddress("1 rue du test");
        cinema = cinemaRepository.saveAndFlush(cinema);

        Seance seance = new Seance();
        seance.setMovie(movie);
        seance.setCinema(cinema);
        seance.setDateTime(LocalDateTime.now().plusDays(2));
        seance.setAvailableSeats(1);
        seance.setPrice(9.50);
        seance.setActive(true);

        return seanceRepository.saveAndFlush(seance).getId();
    }

    /**
     * Creates a distinct test user
     *
     * @param username the test username
     * @return the persisted user identifier
     */
    private Long createUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.test");
        user.setPassword("test-only-password-hash");
        user.setRole(Role.USER);

        return userRepository.saveAndFlush(user).getId();
    }

    /**
     * Waits for a synchronization signal with a bounded timeout
     *
     * @param latch the synchronization latch
     */
    private static void awaitSignal(CountDownLatch latch) {
        try {
            if (!latch.await(20, TimeUnit.SECONDS)) {
                throw new AssertionError("Timed out waiting for concurrency signal");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Concurrency test interrupted", e);
        }
    }


    /**
     * Deletes a screening using the requested administrative mode
     *
     * @param mode the deletion mode
     * @param seanceId the screening identifier
     * @param cinemaId the cinema used for filtered deletion
     */
    private void deleteScreening(DeletionMode mode, Long seanceId, Long cinemaId) {

        switch (mode) {
            case INDIVIDUAL -> seanceService.delete(seanceId);
            case SELECTED -> seanceService.deleteSelected(List.of(seanceId));
            case FILTERED -> seanceService.deleteByAdminFilters(null, cinemaId, true);
        }
    }

}
