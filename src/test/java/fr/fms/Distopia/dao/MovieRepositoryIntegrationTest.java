package fr.fms.Distopia.dao;

import fr.fms.Distopia.entities.Cinema;
import fr.fms.Distopia.entities.Movie;
import fr.fms.Distopia.entities.Seance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for public movie repository queries
 *
 * <p>Uses H2 to verify that movie visibility respects cinema and screening lifecycle states</p>
 */
@DataJpaTest
@ActiveProfiles("test")
class MovieRepositoryIntegrationTest {
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private CinemaRepository cinemaRepository;
    @Autowired
    private SeanceRepository seanceRepository;

    @Test
    @DisplayName("public movie search excludes deleted cinemas")
    void publicMovies_ShouldExcludeDeletedCinema() {
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

        List<Movie> movies = movieRepository.findMoviesWithUpcomingSeancesByCinemaId(cinema.getId(), Sort.by("title"));

        assertThat(movies).isEmpty();
    }


    @Test
    @DisplayName("public movie search includes active cinemas ")
    void publicMovies_ShouldIncludeActiveCinema() {
        Movie movie = new Movie();
        movie.setTitle("Film actif");
        movie = movieRepository.saveAndFlush(movie);
        Cinema cinema = new Cinema();
        cinema.setName("Cinéma actif");
        cinema.setAddress("1 rue du test");
        cinema.setDeleted(false);
        cinema = cinemaRepository.saveAndFlush(cinema);

        Seance seance = new Seance();
        seance.setMovie(movie);
        seance.setCinema(cinema);
        seance.setDateTime(LocalDateTime.now().plusDays(2));
        seance.setAvailableSeats(80);
        seance.setPrice(9.50);
        seance.setActive(true);

        seanceRepository.saveAndFlush(seance);

        List<Movie> movies = movieRepository.findMoviesWithUpcomingSeancesByCinemaId(cinema.getId(), Sort.by("title"));

        assertThat(movies).extracting(Movie::getId).containsExactly(movie.getId());
    }

}
