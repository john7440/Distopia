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

import java.time.LocalDateTime;
import java.util.ArrayList;


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
}
