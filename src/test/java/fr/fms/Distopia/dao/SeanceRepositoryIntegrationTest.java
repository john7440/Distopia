package fr.fms.Distopia.dao;

import fr.fms.Distopia.entities.Cinema;
import fr.fms.Distopia.entities.Movie;
import fr.fms.Distopia.entities.Seance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for public screening queries
 *
 * <p>Uses H2 to verify screening visibility against the persisted movie and cinema lifecycle states</p>
 */
@DataJpaTest
@ActiveProfiles("test")
class SeanceRepositoryIntegrationTest {

    @Autowired
    private SeanceRepository seanceRepository;

    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private CinemaRepository cinemaRepository;

    @Test
    @DisplayName("public screening queries exclude deleted cinemas")
    void publicQueries_ShouldExcludeDeletedCinema() {

        Movie movie = new Movie();
        movie.setTitle("Film de test");
        movie = movieRepository.saveAndFlush(movie);

        Cinema cinema = new Cinema();
        cinema.setName("Cinéma supprimé");
        cinema.setAddress("1 rue du test");
        cinema.setDeleted(true);
        cinema = cinemaRepository.saveAndFlush(cinema);

        Seance seance = new Seance();
        seance.setMovie(movie);
        seance.setCinema(cinema);
        seance.setDateTime(LocalDateTime.now().plusDays(2));
        seance.setAvailableSeats(80);
        seance.setPrice(9.50);
        seance.setActive(true);

        seanceRepository.saveAndFlush(seance);

        assertThat(seanceRepository.findUpcomingSeancesByMovie(movie.getId(), PageRequest.of(0, 10)).getContent()).isEmpty();
        assertThat(seanceRepository.findUpcomingActiveByMovieAndCinema(movie.getId(), cinema.getId(), LocalDateTime.now())).isEmpty();
    }
}
