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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

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
        cinema.setWebsite("www.test.com");
        cinema.setLatitude(1D);
        cinema.setLongitude(1D);
        cinema.setImageUrl("testImage");
        cinema.setDepartment("74");
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


    //---------------------------tests du save()--------------------

    @Test
    @DisplayName("save() - creates a new cinema when id is null")
    void save_ShouldCreateNewCinemaWhenIdIsNull() {
        when(townRepository.findById(1L)).thenReturn(Optional.of(town));
        when(cinemaRepository.save(any(Cinema.class))).thenAnswer(i -> i.getArgument(0));

        Cinema result = cinemaService.save(null, "Nouveau Ciné", "2 rue du test", 1L,
                "www.test.com", 1D, 1D, "testImage", "74");

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

        Cinema result = cinemaService.save(1L, "Ciné update", "3 rue de la modif", 1L,
                "www.test.com", 1D, 1D, "testImage", "74");

        assertThat(result.getName()).isEqualTo("Ciné update");
        assertThat(result.getAddress()).isEqualTo("3 rue de la modif");
        verify(cinemaRepository).save(cinema);
    }

    @Test
    @DisplayName("save() - creates new cinema when id not found in database")
    void save_ShouldCreateNewCinemaWhenIdNotFoundInDatabase() {
        when(cinemaRepository.findById(99L)).thenReturn(Optional.empty());
        when(cinemaRepository.save(any(Cinema.class))).thenAnswer(i -> i.getArgument(0));

        Cinema result = cinemaService.save(99L, "Ghost Ciné", "Nul part", null,
                "www.test.com", 1D, 1D, "testImage", "74");

        assertThat(result.getName()).isEqualTo("Ghost Ciné");
        assertThat(result.getTown()).isNull();
    }

    @Test
    @DisplayName("save() - does not set town when townId is null")
    void save_ShouldNotSetTownWhenTownIdIsNull() {
        when(cinemaRepository.save(any(Cinema.class))).thenAnswer(i -> i.getArgument(0));

        Cinema result = cinemaService.save(null, "Sans Ville", "Adresse", null,
                "www.test.com", 1D, 1D, "testImage", "74");

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

    //---------------------tests for getAllDepartments() --------------------------
    @Test
    @DisplayName("getAllDepartments() - returns all distinct departments")
    void getAllDepartments_ShouldReturnDistinctDepartments() {
        when(cinemaRepository.findDistinctDepartments()).thenReturn(List.of("40","74","57"));

        List<String> result = cinemaService.getAllDepartments();

        assertThat(result).containsExactlyInAnyOrder("40","74","57");
    }

    @Test
    @DisplayName("getAllDepartments() - returns empty list when no departments exist")
    void getAllDepartments_ShouldReturnEmptyListWhenNoDepartmentsExist() {
        when(cinemaRepository.findDistinctDepartments()).thenReturn(List.of());

        List<String> result = cinemaService.getAllDepartments();

        assertThat(result).isEmpty();
    }

    //----------------------------tests for searchPublic()--------------------------
    @Test
    @DisplayName("searchPublic() - uses all filters when keyword town and department are provided")
    void searchPublic_ShouldUseAllFiltersWhenKeywordTownAndDepartmentProvided() {
        Page<Cinema> page = new PageImpl<>(List.of(cinema));

        when(cinemaRepository.searchByAllFilters(eq(1L), eq("40"), eq("Gaumont"), any(Pageable.class)))
                .thenReturn(page);

        Page<Cinema> result = cinemaService.searchPublic("Gaumont", 1L, "40", 0);

        assertThat(result.getContent()).containsExactly(cinema);
        verify(cinemaRepository).searchByAllFilters(eq(1L), eq("40"), eq("Gaumont"),
                any(Pageable.class));
    }

    @Test
    @DisplayName("searchPublic() - uses town and keyword filters")
    void searchPublic_ShouldUseTownAndKeywordFilters() {
        Page<Cinema> page = new PageImpl<>(List.of(cinema));

        when(cinemaRepository.searchByTownAndKeyword(eq(1L), eq("Gaumont"),any(Pageable.class)))
                .thenReturn(page);

        cinemaService.searchPublic("Gaumont", 1L, null, 0);

        verify(cinemaRepository).searchByTownAndKeyword(eq(1L), eq("Gaumont"),any(Pageable.class));
    }

    @Test
    @DisplayName("searchPublic() - uses department and keyword filters")
    void searchPublic_ShouldUseDepartmentAndKeywordFilters() {
        Page<Cinema> page = new PageImpl<>(List.of(cinema));

        when(cinemaRepository.searchByDepartmentAndKeyword(eq("40"),  eq("Gaumont"),any(Pageable.class)))
        .thenReturn(page);

        cinemaService.searchPublic("Gaumont", null, "40", 0);

        verify(cinemaRepository).searchByDepartmentAndKeyword(eq("40"),  eq("Gaumont"),any(Pageable.class));
    }

    @Test
    @DisplayName("searchPublic() - uses town and department filters")
    void searchPublic_ShouldUseTownAndDepartmentFilters() {
        Page<Cinema> page = new PageImpl<>(List.of(cinema));

        when(cinemaRepository.findByTownIdAndDepartment(eq(1L), eq("40"), any(Pageable.class)))
            .thenReturn(page);

        cinemaService.searchPublic(null, 1L, "40", 0);

        verify(cinemaRepository).findByTownIdAndDepartment(eq(1L), eq("40"), any(Pageable.class));
    }

    @Test
    @DisplayName("searchPublic() - uses keyword filter only")
    void searchPublic_ShouldUseKeywordFilterOnly() {
        Page<Cinema> page = new PageImpl<>(List.of(cinema));

        when(cinemaRepository.searchPublic(eq("Gaumont"), any(Pageable.class))).thenReturn(page);

        cinemaService.searchPublic("Gaumont", null, null, 0);

        verify(cinemaRepository).searchPublic(eq("Gaumont"), any(Pageable.class));
    }

    @Test
    @DisplayName("searchPublic() - uses town filter only")
    void searchPublic_ShouldUseTownFilterOnly() {
        Page<Cinema> page = new PageImpl<>(List.of(cinema));

        when(cinemaRepository.findByTownId(eq(1L), any(Pageable.class)))
                .thenReturn(page);

        cinemaService.searchPublic(null,1L, null, 0);

        verify(cinemaRepository).findByTownId(eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("searchPublic() - uses department filter only")
    void searchPublic_ShouldUseDepartmentFilterOnly() {
        Page<Cinema> page = new PageImpl<>(List.of(cinema));
        when(cinemaRepository.findByDepartment(eq("40"), any(Pageable.class)))
                .thenReturn(page);

        cinemaService.searchPublic(null, null,"40",0);

        verify(cinemaRepository).findByDepartment(eq("40"), any(Pageable.class));
    }

}
