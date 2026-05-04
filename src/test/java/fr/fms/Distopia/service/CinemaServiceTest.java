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
import static org.mockito.Mockito.*;


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

    //------------------------test du getByTown()-------------------
    @Test
    @DisplayName("getByTown() - calls findByTownId() Repository")
    void getByTownId_ShouldCallFindByTownIdRepository() {
        cinemaService.getByTown(1L);

        verify(cinemaRepository).findByTownId(1L);
    }

    //------------------------test du findById()-------------------
    @Test
    @DisplayName("findById() - calls findById() Repository")
    void findById_ShouldCallFindByIdRepository() {
        cinemaService.findById(1L);

        verify(cinemaRepository).findById(1L);
    }

    //------------------------test du getAll()-------------------
    @Test
    @DisplayName("getAll() - calls findAll() Repository")
    void getAll_ShouldCallFindAllRepository() {
        cinemaService.getAll();

        verify(cinemaRepository).findAll();
    }

    //---------------------tests du search()----------------------------------
    @Test
    @DisplayName("search() - search with keyword and town should call correct Repo")
    void search_WithKeywordAndTownShouldCallCorrectRepo() {
        cinemaService.search("cinema", 1L);

        verify(cinemaRepository).findByTownIdAndNameContainingIgnoreCaseOrTownIdAndAddressContainingIgnoreCase(
                1L,"cinema", 1L,"cinema"
        );
    }

    @Test
    @DisplayName("search() - search with keyword only should call Keyword Repo")
    void search_WithKeywordOnlyShouldCallKeywordRepo() {
        cinemaService.search("cinema", null);

        verify(cinemaRepository).findByNameContainingIgnoreCaseOrAddressContainingIgnoreCase(
                "cinema", "cinema"
        );
    }

    @Test
    @DisplayName("search() - with town only should call Town Repo")
    void search_WithTownOnlyShouldCallTownRepo() {
        cinemaService.search(null, 1L);

        verify(cinemaRepository).findByTownId(1L);
    }

    @Test
    @DisplayName("search() - with nothing should returns all")
    void search_WithNothingShouldReturnsAll() {
        cinemaService.search(null, null);

        verify(cinemaRepository).findAll();
    }

    //---------------------------tests du save()--------------------

    @Test
    @DisplayName("save() - creates a new cinema when id is null")
    void save_ShouldCreateNewCinemaWhenIdIsNull() {
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
    void save_ShouldUpdateExistingCinemaWhenIdIsFound() {
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
    void save_ShouldCreateNewCinemaWhenIdNotFoundInDatabase() {
        when(cinemaRepository.findById(99L)).thenReturn(Optional.empty());
        when(cinemaRepository.save(any(Cinema.class))).thenAnswer(i -> i.getArgument(0));

        Cinema result = cinemaService.save(99L, "Ghost Ciné", "Nul part", null);

        assertThat(result.getName()).isEqualTo("Ghost Ciné");
        assertThat(result.getTown()).isNull();
    }

    @Test
    @DisplayName("save() - does not set town when townId is null")
    void save_ShouldNotSetTownWhenTownIdIsNull() {
        when(cinemaRepository.save(any(Cinema.class))).thenAnswer(i -> i.getArgument(0));

        Cinema result = cinemaService.save(null, "Sans Ville", "Adresse", null);

        assertThat(result.getTown()).isNull();
        verify(townRepository, never()).findById(any());
    }

    //---------------------------tests du delete()---------------------------

    @Test
    @DisplayName("delete() - deletes cinema and soft-deletes orphaned movies")
    void delete_ShouldDeleteCinemaAndSoftDeletesOrphanedMovies() {
        when(cinemaRepository.findById(1L)).thenReturn(Optional.of(cinema));

        cinemaService.delete(1L);

        assertThat(movie.isDeleted()).isTrue();
        verify(movieRepository).save(movie);
        verify(cinemaRepository).delete(cinema);
    }

    @Test
    @DisplayName("delete() - does not soft-delete movie when it still has other cinemas")
    void delete_ShouldNotSoftDeleteMoviesWhenMovieHasOtherCinemas() {
        Cinema anotherCinema = new Cinema();
        anotherCinema.setId(2L);
        movie.getCinemas().add(anotherCinema);

        when(cinemaRepository.findById(1L)).thenReturn(Optional.of(cinema));

        cinemaService.delete(1L);

        assertThat(movie.isDeleted()).isFalse();
        verify(cinemaRepository).delete(cinema);
    }

    @Test
    @DisplayName("delete() - does nothing when cinema id is not found")
    void delete_ShouldDoNothingWhenCinemaIdIsNotFound() {
        when(cinemaRepository.findById(99L)).thenReturn(Optional.empty());

        cinemaService.delete(99L);

        verify(cinemaRepository, never()).delete(any());
        verify(movieRepository, never()).save(any());
    }

}
