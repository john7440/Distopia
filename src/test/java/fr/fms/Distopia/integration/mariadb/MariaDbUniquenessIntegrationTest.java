package fr.fms.Distopia.integration.mariadb;

import fr.fms.Distopia.dao.CinemaRepository;
import fr.fms.Distopia.dao.MovieRepository;
import fr.fms.Distopia.dao.SeanceRepository;
import fr.fms.Distopia.entities.Cinema;
import fr.fms.Distopia.entities.Movie;
import fr.fms.Distopia.entities.Seance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies database uniqueness constraints against MariaDB
 *
 * <p>Concurrent insertions must not create duplicate TMDB
 * movies or duplicate screening slots </p>
 */
@DataJpaTest
@ActiveProfiles("mariadb-test")
@TestPropertySource(locations = "classpath:application-mariadb-test.properties")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnabledIfSystemProperty(named = "distopia.mariadb.tests", matches = "true")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class MariaDbUniquenessIntegrationTest {

    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private CinemaRepository cinemaRepository;
    @Autowired
    private SeanceRepository seanceRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    /**
     * Verifies that concurrent insertions cannot create two movies with the same TMDB identifier
     */
    @Test
    @DisplayName("MariaDB - rejects concurrent duplicate TMDB movies")
    void movies_ShouldRejectConcurrentDuplicateTmdbId() throws InterruptedException, ExecutionException, TimeoutException {
        Long tmdbId = 987654L;

        Long persistedId = assertConcurrentDuplicateRejected(() -> createMovie("Original movie", tmdbId), () -> createMovie("Duplicate movie", tmdbId));
        Long movieCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM movie WHERE tmdb_id = ?", Long.class, tmdbId);

        assertThat(movieCount).isEqualTo(1L);

        Movie persistedMovie = movieRepository.findById(persistedId).orElseThrow();

        assertThat(persistedMovie.getTitle()).isEqualTo("Original movie");
        assertThat(persistedMovie.getTmdbId()).isEqualTo(tmdbId);
    }

    /**
     * Verifies that concurrent insertions cannot create two screenings for the same movie, cinema and date
     *
     * <p>An inactive screening must also prevent the creation of a duplicate active screening</p>
     */
    @Test
    @DisplayName("MariaDB - rejects concurrent duplicate screening slots")
    void screenings_ShouldRejectConcurrentDuplicateSlot() throws InterruptedException, ExecutionException, TimeoutException {
        Movie movie = new Movie();
        movie.setTitle("Screening uniqueness movie");

        Movie savedMovie = movieRepository.saveAndFlush(movie);

        Cinema cinema = new Cinema();
        cinema.setName("Screening uniqueness cinema");
        cinema.setAddress("1 rue du test");

        Cinema savedCinema = cinemaRepository.saveAndFlush(cinema);

        LocalDateTime slot = LocalDate.now().plusDays(2).atTime(14, 0);

        Long persistedId = assertConcurrentDuplicateRejected(() -> createScreening(savedMovie, savedCinema, slot, false),
                () -> createScreening(savedMovie, savedCinema, slot, true));

        Long screeningCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM seance
                WHERE movie_id = ?
                  AND cinema_id = ?
                  AND date_time = ?
                """,
                Long.class,
                savedMovie.getId(),
                savedCinema.getId(),
                Timestamp.valueOf(slot)
        );

        assertThat(screeningCount).isEqualTo(1L);

        Seance persistedSeance = seanceRepository.findById(persistedId).orElseThrow();

        assertThat(persistedSeance.isActive()).isFalse();
        assertThat(persistedSeance.getAvailableSeats()).isEqualTo(100);
    }

    /**
     * Executes two conflicting insertions in independent transactions and verifies that the duplicate is rejected
     *
     * @param firstInsert the initial insertion
     * @param duplicateInsert the conflicting insertion
     * @return the identifier of the successfully persisted entity
     */
    private Long assertConcurrentDuplicateRejected(Supplier<Long> firstInsert, Supplier<Long> duplicateInsert)
            throws InterruptedException, ExecutionException, TimeoutException {

        CountDownLatch firstInserted = new CountDownLatch(1);
        CountDownLatch allowFirstCommit = new CountDownLatch(1);
        CountDownLatch duplicateStarted = new CountDownLatch(1);

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        boolean executorTerminated;
        Long persistedId;

        try {
            Future<Long> first = executor.submit(() ->
                    transactionTemplate.execute(status -> {
                        Long id = firstInsert.get();

                        firstInserted.countDown();
                        awaitSignal(allowFirstCommit);

                        return id;
                    })
            );

            awaitSignal(firstInserted);

            Future<Long> duplicate = executor.submit(() -> {
                duplicateStarted.countDown();
                return duplicateInsert.get();
            });

            awaitSignal(duplicateStarted);

            assertThatThrownBy(() -> duplicate.get(1, TimeUnit.SECONDS)).isInstanceOf(TimeoutException.class);

            allowFirstCommit.countDown();

            persistedId = first.get(20, TimeUnit.SECONDS);

            assertThat(persistedId).isNotNull();
            assertThatThrownBy(() -> duplicate.get(20, TimeUnit.SECONDS)).isInstanceOf(ExecutionException.class).hasCauseInstanceOf(DataIntegrityViolationException.class);

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

        assertThat(executorTerminated).as("Concurrent insertion tasks must terminate").isTrue();

        return persistedId;
    }

    /**
     * Persists a movie with the requested TMDB identifier
     *
     * @param title the movie title
     * @param tmdbId the TMDB identifier
     * @return the persisted movie identifier
     */
    private Long createMovie(String title, Long tmdbId) {
        Movie movie = new Movie();
        movie.setTitle(title);
        movie.setTmdbId(tmdbId);

        return movieRepository.saveAndFlush(movie).getId();
    }

    /**
     * Persists a screening with the requested slot and activity
     *
     * @param movie the associated movie
     * @param cinema the associated cinema
     * @param slot the screening date and time
     * @param active whether the screening is active
     * @return the persisted screening identifier
     */
    private Long createScreening(Movie movie, Cinema cinema, LocalDateTime slot, boolean active) {
        Seance seance = new Seance();
        seance.setMovie(movie);
        seance.setCinema(cinema);
        seance.setDateTime(slot);
        seance.setAvailableSeats(100);
        seance.setPrice(9.50);
        seance.setActive(active);

        return seanceRepository.saveAndFlush(seance).getId();
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

            throw new IllegalStateException("Concurrency test interrupted", e);
        }
    }
}
