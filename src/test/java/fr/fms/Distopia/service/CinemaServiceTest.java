package fr.fms.Distopia.service;

import fr.fms.Distopia.dao.CinemaRepository;
import fr.fms.Distopia.dao.MovieRepository;
import fr.fms.Distopia.dao.TownRepository;
import fr.fms.Distopia.entities.Cinema;
import fr.fms.Distopia.entities.Movie;
import fr.fms.Distopia.entities.Town;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class CinemaServiceTest {

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

    //---------------------------tests du save()--------------------

    @Test
    @DisplayName("save() - creates a new cinema when id is null")
    void saveShouldCreateNewCinemaWhenIdIsNull() {
        when(townRepository.findById(1L)).thenReturn(Optional.of(town));
        when(cinemaRepository.save(any(Cinema.class))).thenAnswer(i -> i.getArgument(0));

        Cinema result = cinemaService.save(null, "Nouveau Ciné", "2 rue du test", 1L);

        assertThat(result.getName()).isEqualTo("Nouveau Ciné");
        assertThat(result.getAddress()).isEqualTo("2 rue du test");
        assertThat(result.getTown()).isEqualTo(town);
        verify(cinemaRepository).save(any(Cinema.class));
    }

    @Test
    @DisplayName("save() - updates existing cinema when id is found")
    void saveShouldUpdateExistingCinemaWhenIdIsFound() {
        when(cinemaRepository.findById(1L)).thenReturn(Optional.of(cinema));
        when(townRepository.findById(1L)).thenReturn(Optional.of(town));
        when(cinemaRepository.save(any(Cinema.class))).thenAnswer(i -> i.getArgument(0));

        Cinema result = cinemaService.save(1L, "Ciné update", "3 rue de la modif", 1L);

        assertThat(result.getName()).isEqualTo("Ciné update");
        assertThat(result.getAddress()).isEqualTo("3 rue de la modif");
        verify(cinemaRepository).save(cinema);
    }

    @Test
    @DisplayName("save() - creates new cinema when id not found in database")
    void  saveShouldCreateNewCinemaWhenIdNotFoundInDatabase() {
        when(cinemaRepository.findById(99L)).thenReturn(Optional.empty());
        when(cinemaRepository.save(any(Cinema.class))).thenAnswer(i -> i.getArgument(0));

        Cinema result = cinemaService.save(99L, "Ghost Ciné", "Nul part", null);

        assertThat(result.getName()).isEqualTo("Ghost Ciné");
        assertThat(result.getTown()).isNull();
    }

}
