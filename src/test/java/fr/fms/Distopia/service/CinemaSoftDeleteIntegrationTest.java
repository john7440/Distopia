package fr.fms.Distopia.service;

import fr.fms.Distopia.dao.CinemaRepository;
import fr.fms.Distopia.dao.MovieRepository;
import fr.fms.Distopia.dao.SeanceRepository;
import fr.fms.Distopia.entities.Cinema;
import fr.fms.Distopia.entities.Movie;
import fr.fms.Distopia.entities.Seance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for cinema soft deletion
 *
 * <p>Uses H2 to verify persisted screening state after the cinema deletion transaction has completed</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class CinemaSoftDeleteIntegrationTest {

    @Autowired
    private CinemaService cinemaService;

    @Autowired
    private CinemaRepository cinemaRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private SeanceRepository seanceRepository;

    /**
     * Verifies that soft deletion persists the cinema deletion flag, deactivates future screenings and preserves seat counts
     *
     * <p>The test runs without an enclosing test transaction so that database reads occur after the service transaction
     * has committed</p>
     */
    @Test
    @DisplayName("soft delete persists inactive screenings without losing seats")
    void delete_shouldPersistInactiveScreeningAndPreserveSeats() {
        // GIVEN
        Movie movie = new Movie();
        movie.setTitle("Film intégration");
        movie = movieRepository.saveAndFlush(movie);

        Cinema cinema = new Cinema();
        cinema.setName("Cinéma intégration");
        cinema.setAddress("1 rue du test");
        cinema = cinemaRepository.saveAndFlush(cinema);

        Seance seance = new Seance();
        seance.setMovie(movie);
        seance.setCinema(cinema);
        seance.setDateTime(LocalDateTime.now().plusDays(2));
        seance.setAvailableSeats(80);
        seance.setPrice(9.50);
        seance.setActive(true);

        seance = seanceRepository.saveAndFlush(seance);

        Long cinemaId = cinema.getId();
        Long seanceId = seance.getId();

        cinemaService.delete(cinemaId);
        Cinema savedCinema = cinemaRepository.findById(cinemaId).orElseThrow();

        Seance savedSeance = seanceRepository.findById(seanceId).orElseThrow();

        assertThat(savedCinema.isDeleted()).isTrue();
        assertThat(savedSeance.isActive()).isFalse();
        assertThat(savedSeance.getAvailableSeats()).isEqualTo(80);
    }
}
