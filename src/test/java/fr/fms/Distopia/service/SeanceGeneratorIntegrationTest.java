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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for screening generation
 *
 * <p>Uses a real test database to verify persistence
 * and screening lifecycle invariants</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SeanceGeneratorIntegrationTest {

    @Autowired
    private SeanceGeneratorService generatorService;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private CinemaRepository cinemaRepository;

    @Autowired
    private SeanceRepository seanceRepository;

    @Test
    @DisplayName("generation must not recreate an inactive screening")
    void generation_shouldNotDuplicateInactiveScreening() {

        Movie movie = new Movie();
        movie.setTitle("Film de test");
        movie = movieRepository.saveAndFlush(movie);

        Cinema cinema = new Cinema();
        cinema.setName("Cinema de test");
        cinema.setAddress("1 rue du test");
        cinema.getMovies().add(movie);
        cinema = cinemaRepository.saveAndFlush(cinema);

        movie.getCinemas().add(cinema);

        LocalDateTime screeningDate = LocalDate.now().plusDays(1).atTime(14, 0);

        Seance existing = new Seance();
        existing.setMovie(movie);
        existing.setCinema(cinema);
        existing.setDateTime(screeningDate);
        existing.setAvailableSeats(100);
        existing.setPrice(9.0);
        existing.setActive(false);

        existing = seanceRepository.saveAndFlush(existing);
        Long existingId = existing.getId();

        SeanceGeneratorService.GeneratorResult result = generatorService.generateForMovie(movie);

        assertThat(result.seancesCreated()).isEqualTo(20);
        assertThat(seanceRepository.findAll()).hasSize(21);
        assertThat(seanceRepository.findById(existingId).orElseThrow().isActive()).isFalse();
        SeanceGeneratorService.GeneratorResult secondRun = generatorService.generateForMovie(movie);
        assertThat(secondRun.seancesCreated()).isZero();
        assertThat(seanceRepository.count()).isEqualTo(21);
    }
}
