package fr.fms.Distopia.service;

import fr.fms.Distopia.dao.CinemaRepository;
import fr.fms.Distopia.dao.MovieRepository;
import fr.fms.Distopia.dao.TownRepository;
import fr.fms.Distopia.entities.Cinema;
import fr.fms.Distopia.entities.Movie;
import fr.fms.Distopia.entities.Town;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;


@ExtendWith(MockitoExtension.class)
public class CinemaServiceTest {

    @Mock
    private CinemaRepository cinemaRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private TownRepository townRepository;

    @InjectMocks
    private CinemaService cinemaService;

    private Cinema cinema;
    private Movie movie;
    private Town town;

    @BeforeEach
    void setup() {
        town = new Town();
        town.setId(1L);
        town.setName("Paris");

        cinema = new Cinema();
        cinema.setId(1L);
        cinema.setName("Gaumont");
        cinema.setTown(town);
        cinema.setAddress("1 rue de la Paix");
        cinema.setMovies(new ArrayList<>());

        movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Inception");
        movie.setDeleted(false);
        List<Cinema> cinemas = new ArrayList<>();
        cinemas.add(cinema);
        movie.setCinemas(cinemas);
        cinema.getMovies().add(movie);

    }


}
