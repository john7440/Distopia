package fr.fms.Distopia.service;

import fr.fms.Distopia.dao.CinemaRepository;
import fr.fms.Distopia.dao.MovieRepository;
import fr.fms.Distopia.dao.SeanceRepository;
import fr.fms.Distopia.entities.Cinema;
import fr.fms.Distopia.entities.Movie;
import fr.fms.Distopia.entities.Seance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;
    @Mock
    private CinemaRepository cinemaRepository;
    @Mock
    private SeanceRepository seanceRepository;
    @InjectMocks
    private MovieService movieService;

    private Movie movie;
    private Cinema cinema;
    private Seance seance;

    @BeforeEach
    void setUp() {
        cinema = new Cinema();
        cinema.setId(1L);
        cinema.setName("Cinema Test");

        seance = new Seance();
        seance.setId(1L);
        seance.setAvailableSeats(100);

        movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Movie Test");
        movie.setDeleted(false);
        movie.setSeances(new ArrayList<>(List.of(seance)));
        movie.setCinemas(new ArrayList<>(List.of(cinema)));
    }
}
