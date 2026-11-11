package fr.fms.Distopia.service;

import fr.fms.Distopia.dao.CinemaRepository;
import fr.fms.Distopia.dao.TownRepository;
import fr.fms.Distopia.entities.Cinema;
import fr.fms.Distopia.entities.Seance;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class CinemaServiceTest {

    @Mock
    private CinemaRepository cinemaRepository;
    @Mock
    private TownRepository townRepository;

    @InjectMocks
    private CinemaService cinemaService;

    private Cinema cinema;
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
        cinema.setDeleted(false);
        cinema.setMovies(new ArrayList<>());
        cinema.setSeances(new ArrayList<>());
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
    @DisplayName("getAll() - calls findByDeletedFalse() Repository")
    void getAll_ShouldCallFindAllRepository() {
        cinemaService.getAll();

        verify(cinemaRepository).findByDeletedFalse();
        verify(cinemaRepository, never()).findAll();
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


    @Test
    @DisplayName("save() - preserves deleted status when updating a cinema")
    void save_ShouldPreserveDeletedStatusWhenUpdating() {
        cinema.setDeleted(true);

        when(cinemaRepository.findById(1L)).thenReturn(Optional.of(cinema));

        when(cinemaRepository.save(any(Cinema.class))).thenAnswer(inv -> inv.getArgument(0));

        Cinema result = cinemaService.save(1L, "Cinéma renommé", "2 rue du test", null, "https://example.com",
                43.48, -1.57, "image.jpg", "40");

        assertThat(result.isDeleted()).isTrue();
        assertThat(result.getName()).isEqualTo("Cinéma renommé");
        assertThat(result.getAddress()).isEqualTo("2 rue du test");

        verify(cinemaRepository).save(cinema);
    }

    @Test
    @DisplayName("save() - creates new cinemas  as active")
    void save_ShouldCreateNewCinemaAsActive() {
        when(cinemaRepository.save(any(Cinema.class))).thenAnswer(inv -> inv.getArgument(0));

        Cinema result = cinemaService.save(null, "Nouveau cinéma", "1 rue du test", null,
                "https://example.com", 43.48, -1.57, "image.jpg", "40");

        assertThat(result.isDeleted()).isFalse();
        assertThat(result.getName()).isEqualTo("Nouveau cinéma");
        verify(cinemaRepository).save(any(Cinema.class));
    }



    //---------------------------tests du delete()---------------------------

    @Test
    @DisplayName("delete() - soft-deletes cinema")
    void delete_ShouldSoftDeleteCinema() {
        when(cinemaRepository.findById(1L)).thenReturn(Optional.of(cinema));

        cinemaService.delete(1L);

        assertThat(cinema.isDeleted()).isTrue();

        verify(cinemaRepository).save(cinema);
        verify(cinemaRepository, never()).delete(any());

    }

    @Test
    @DisplayName("delete() - ignores seances without date")
    void delete_ShouldIgnoreSeancesWithoutDate() {
        Seance seanceWithoutDate = new Seance();
        seanceWithoutDate.setDateTime(null);
        seanceWithoutDate.setActive(true);
        seanceWithoutDate.setAvailableSeats(20);

        cinema.setSeances(List.of(seanceWithoutDate));

        when(cinemaRepository.findById(1L)).thenReturn(Optional.of(cinema));

        cinemaService.delete(1L);

        assertThat(cinema.isDeleted()).isTrue();
        assertThat(seanceWithoutDate.isActive()).isTrue();
        assertThat(seanceWithoutDate.getAvailableSeats()).isEqualTo(20);

        verify(cinemaRepository).save(cinema);
        verify(cinemaRepository, never()).delete(any());
    }

    @Test
    @DisplayName("delete() - does nothing when cinema id is not found")
    void delete_ShouldDoNothingWhenCinemaIdIsNotFound() {
        when(cinemaRepository.findById(99L)).thenReturn(Optional.empty());

        cinemaService.delete(99L);

        verify(cinemaRepository, never()).save(any());
        verify(cinemaRepository, never()).delete(any());
    }


    /**
     * Verifies that soft-deleting a cinema disables future screenings
     * without destroying their remaining seat counts.
     */
    @Test
    @DisplayName("delete() - preserves available seats of future screenings")
    void delete_ShouldPreserveAvailableSeatsOfFutureSeances() {
        // GIVEN
        Seance futureSeance = new Seance();
        futureSeance.setDateTime(LocalDateTime.now().plusDays(1));
        futureSeance.setActive(true);
        futureSeance.setAvailableSeats(80);

        cinema.getSeances().add(futureSeance);

        when(cinemaRepository.findById(1L))
                .thenReturn(Optional.of(cinema));

        // WHEN
        cinemaService.delete(1L);

        // THEN
        assertThat(cinema.isDeleted()).isTrue();
        assertThat(futureSeance.isActive()).isFalse();
        assertThat(futureSeance.getAvailableSeats()).isEqualTo(80);

        verify(cinemaRepository).save(cinema);
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
        when(cinemaRepository.findByDepartmentAndDeletedFalse(eq("40"), any(Pageable.class)))
                .thenReturn(page);

        cinemaService.searchPublic(null, null,"40",0);

        verify(cinemaRepository).findByDepartmentAndDeletedFalse(eq("40"), any(Pageable.class));
    }

    @Test
    @DisplayName("searchPublic() - returns available cinemas when no filters are provided")
    void searchPublic_ShouldReturnAvailableCinemasWhenNoFiltersProvided() {
        Page<Cinema> page = new PageImpl<>(List.of(cinema));

        when(cinemaRepository.findByDeletedFalse(any(Pageable.class))).thenReturn(page);

        Page<Cinema> result = cinemaService.searchPublic(null, null, null, 0);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).containsExactly(cinema);

        verify(cinemaRepository).findByDeletedFalse(any(Pageable.class));
        verify(cinemaRepository, never()).findAll(any(Pageable.class));
    }

    //-------------------------tests for searchAdmin()  --------------------------
    @Test
    @DisplayName("searchAdmin() - searches cinemas when keyword is provided")
    void searchAdmin_ShouldSearchCinemasWhenKeywordProvided() {
        Page<Cinema> page = new PageImpl<>(List.of(cinema));
        when(cinemaRepository.searchAdmin(eq("Gaumont"),any(Pageable.class)))
                .thenReturn(page);

        Page<Cinema> result = cinemaService.searchAdmin("Gaumont","name","asc",0);

        assertThat(result.getContent()).containsExactly(cinema);
        verify(cinemaRepository).searchAdmin(eq("Gaumont"), any(Pageable.class));
    }

    @Test
    @DisplayName("searchAdmin() - returns available cinemas when keyword is blank")
    void searchAdmin_ShouldReturnAvailableCinemasWhenKeywordBlank() {
        Page<Cinema> page = new PageImpl<>(List.of(cinema));

        when(cinemaRepository.findByDeletedFalse(any(Pageable.class))).thenReturn(page);

        Page<Cinema> result = cinemaService.searchAdmin("", "name", "asc", 0);

        assertThat(result.getContent()).containsExactly(cinema);

        verify(cinemaRepository).findByDeletedFalse(any(Pageable.class));
        verify(cinemaRepository, never()).findAll(any(Pageable.class));
    }


    @Test
    @DisplayName("searchAdmin() - includes deleted cinemas when requested")
    void searchAdmin_ShouldIncludeDeletedCinemas() {
        cinema.setDeleted(true);

        Page<Cinema> page = new PageImpl<>(List.of(cinema));

        when(cinemaRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<Cinema> result = cinemaService.searchAdmin(null, "name", "asc", 0, true);

        assertThat(result.getContent()).containsExactly(cinema);
        verify(cinemaRepository).findAll(any(Pageable.class));
        verify(cinemaRepository, never()).findByDeletedFalse(any(Pageable.class));
    }

    @Test
    @DisplayName("searchAdmin() - searches deleted cinemas when requested")
    void searchAdmin_ShouldSearchIncludingDeletedCinemas() {
        cinema.setDeleted(true);

        Page<Cinema> page = new PageImpl<>(List.of(cinema));

        when(cinemaRepository.searchAdminIncludingDeleted(eq("Gaumont"), any(Pageable.class))).thenReturn(page);

        Page<Cinema> result = cinemaService.searchAdmin("Gaumont", "name", "asc", 0, true);

        assertThat(result.getContent()).containsExactly(cinema);
        verify(cinemaRepository).searchAdminIncludingDeleted(eq("Gaumont"), any(Pageable.class));
    }


    //-------------------------tests for restore()--------------------------
    @Test
    @DisplayName(" restore() - restores cinema without changing screenings")
    void restore_ShouldRestoreCinemaWithoutReactivatingSeances() {
        cinema.setDeleted(true);

        Seance seance = new Seance();
        seance.setDateTime(LocalDateTime.now().plusDays(2));
        seance.setActive(false);
        seance.setAvailableSeats(80);
        cinema.getSeances().add(seance);

        when(cinemaRepository.findById(1L)).thenReturn(Optional.of(cinema));

        boolean restored = cinemaService.restore(1L);

        assertThat(restored).isTrue();
        assertThat(cinema.isDeleted()).isFalse();
        assertThat(seance.isActive()).isFalse();
        assertThat(seance.getAvailableSeats()).isEqualTo(80);

        verify(cinemaRepository).save(cinema);
    }


    @Test
    @DisplayName("restore() - does nothing when cinema is already active ")
    void restore_ShouldDoNothingWhenCinemaIsAlreadyActive() {
        // GIVEN
        cinema.setDeleted(false);

        when(cinemaRepository.findById(1L)).thenReturn(Optional.of(cinema));

        boolean restored = cinemaService.restore(1L);

        assertThat(restored).isFalse();
        assertThat(cinema.isDeleted()).isFalse();

        verify(cinemaRepository, never()).save(any(Cinema.class));
    }

    @Test
    @DisplayName("restore() - rejects unknown cinema")
    void restore_ShouldRejectUnknownCinema() {
        when(cinemaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cinemaService.restore(99L)).isInstanceOf(NoSuchElementException.class);

        verify(cinemaRepository, never()).save(any(Cinema.class));
    }

}
