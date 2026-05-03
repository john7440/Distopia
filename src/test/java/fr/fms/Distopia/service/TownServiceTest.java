package fr.fms.Distopia.service;

import fr.fms.Distopia.dao.CinemaRepository;
import fr.fms.Distopia.dao.TownRepository;
import fr.fms.Distopia.entities.Cinema;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TownServiceTest {

    @Mock
    private TownRepository townRepository;
    @Mock
    private CinemaRepository cinemaRepository;
    @InjectMocks
    private TownService townService;

    private Town town;
    private Cinema cinema;

    @BeforeEach
    void setUp() {
        town = new Town();
        town.setId(1L);
        town.setName("Paris");

        cinema = new Cinema();
        cinema.setId(1L);
        cinema.setName("Cinéma Test");
        cinema.setTown(town);

        town.setCinemas(new ArrayList<>(List.of(cinema)));
    }

    //-------------------tests de la méthode save()-------------------
    @Test
    @DisplayName("save() - creates a new town when id is null")
    void save_ShouldCreateNewTownWhenIdIsNull() {
        when(townRepository.save(any(Town.class))).thenAnswer(i -> i.getArgument(0));

        Town result = townService.save(null, "Annecy");

        assertThat(result.getName()).isEqualTo("Annecy");
        verify(townRepository).save(any(Town.class));
    }

    @Test
    @DisplayName("save() - updates existing town when id is found")
    void save_ShouldUpdateExistingTownWhenIdIsFound() {
        when(townRepository.findById(1L)).thenReturn(Optional.of(town));
        when(townRepository.save(any(Town.class))).thenAnswer(i -> i.getArgument(0));

        Town result = townService.save(1L, "Paris updated");

        assertThat(result.getName()).isEqualTo("Paris updated");
        verify(townRepository).save(any(Town.class));
    }

    @Test
    @DisplayName("save() - creates new town when id not found in database")
    void save_ShouldCreateNewTownWhenIdIsNotFound() {
        when(townRepository.findById(99L)).thenReturn(Optional.empty());
        when(townRepository.save(any(Town.class))).thenAnswer(i -> i.getArgument(0));

        Town result = townService.save(99L, "Ville fantôme");

        assertThat(result.getName()).isEqualTo("Ville fantôme");
    }

    //---------------------test de la méthode delete()------------------------------------
    @Test
    @DisplayName("delete() - sets cinema town to null before deleting town")
    void delete_shouldNullifyAssociatedCinemasTown_beforeDeletion() {
        when(townRepository.findById(1L)).thenReturn(Optional.of(town));

        townService.delete(1L);

        assertThat(cinema.getTown()).isNull();
        verify(cinemaRepository).save(cinema);
        verify(townRepository).delete(town);
    }

    @Test
    @DisplayName("delete() - does nothing when town id not found")
    void delete_shouldDoNothing_whenTownNotFound() {
        when(townRepository.findById(99L)).thenReturn(Optional.empty());

        townService.delete(99L);

        verify(cinemaRepository, never()).save(any());
        verify(townRepository, never()).delete(any());
    }

    @Test
    @DisplayName("delete() - handles town with no cinemas gracefully")
    void delete_shouldDeleteTown_whenNoCinemasAssociated() {
        town.setCinemas(new ArrayList<>());
        when(townRepository.findById(1L)).thenReturn(Optional.of(town));

        townService.delete(1L);

        verify(cinemaRepository, never()).save(any());
        verify(townRepository).delete(town);
    }
}
