package fr.fms.Distopia.web;


import fr.fms.Distopia.dao.TownRepository;
import fr.fms.Distopia.entities.Role;
import fr.fms.Distopia.entities.Town;
import fr.fms.Distopia.entities.User;
import fr.fms.Distopia.service.TownService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TownControllerTest {
    @Mock
    private TownService townService;

    @Mock
    private TownRepository townRepository;

    @Mock
    private HttpSession session;

    @Mock
    private Model model;

    @InjectMocks
    private TownController townController;

    private User adminUser;
    private User regularUser;
    private Town town;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setRole(Role.ADMIN);

        regularUser = new User();
        regularUser.setId(2L);
        regularUser.setRole(Role.USER);

        town = new Town();
        town.setId(1L);
        town.setName("Dax");
    }

    //---------------------------test for towns()--------------------
    @Test
    @DisplayName("towns() - returns 'admin-towns' view for admin user")
    void towns_ShouldReturnAdminTownsViewForAdminUser() {
        when(session.getAttribute("connectedUser")).thenReturn(adminUser);
        when(townService.getAll()).thenReturn(List.of(town));

        String view = townController.towns(null,model,session);

        assertThat(view).isEqualTo("admin-towns");
    }

    @Test
    @DisplayName("towns() - redirects to index view non admin user")
    void towns_ShouldRedirectToIndexViewNonAdminUser() {
        when(session.getAttribute("connectedUser")).thenReturn(regularUser);

        String view = townController.towns(null,model,session);

        assertThat(view).isEqualTo("redirect:/index");
    }

    @Test
    @DisplayName("towns() - redirects if no current user")
    void towns_ShouldRedirectIfNoCurrentUser() {
        when(session.getAttribute("connectedUser")).thenReturn(null);

        String view = townController.towns(null,model,session);

        assertThat(view).isEqualTo("redirect:/index");
    }

    @Test
    @DisplayName("towns() - adds all towns to model")
    void towns_ShouldAddAllTownsToModel() {
        when(session.getAttribute("connectedUser")).thenReturn(adminUser);
        when(townService.getAll()).thenReturn(List.of(town));

        townController.towns(null,model,session);

        verify(model).addAttribute("towns", List.of(town));
    }

    @Test
    @DisplayName("towns() - adds editTown to model when editId is found")
    void towns_ShouldAddEitTownToModelWhenEditIdIsFound() {
        when(session.getAttribute("connectedUser")).thenReturn(adminUser);
        when(townService.getAll()).thenReturn(List.of(town));
        when(townRepository.findById(1L)).thenReturn(Optional.of(town));

        townController.towns(1L,model,session);

        verify(model).addAttribute("editTown", town);
    }

    @Test
    @DisplayName("towns() - does not add editTown to model when editId is not found")
    void towns_ShouldNotAddEitTownToModelWhenEditIdIsNotFound() {
        when(session.getAttribute("connectedUser")).thenReturn(adminUser);
        when(townService.getAll()).thenReturn(List.of(town));
        when(townRepository.findById(99L)).thenReturn(Optional.empty());

        townController.towns(99L,model,session);

        verify(model, never()).addAttribute("editTown", town);
    }

    //-----------------------tests for saveTown()-----------------------------------
    @Test
    @DisplayName("saveTown() - saves town and redirects for admin user")
    void saveTown_ShouldSaveTownAndRedirectsForAdminUser() {
        when(session.getAttribute("connectedUser")).thenReturn(adminUser);

        String view = townController.saveTown(null,"Paris", session);

        assertThat(view).isEqualTo("redirect:/admin/towns");
        verify(townService).save(null, "Paris");
    }

    @Test
    @DisplayName("saveTown() - redirect to index non admin user")
    void saveTown_ShouldRedirectToIndexNonAdminUser() {
        when(session.getAttribute("connectedUser")).thenReturn(regularUser);

        String view = townController.saveTown(null,"Paris", session);

        assertThat(view).isEqualTo("redirect:/index");
        verify(townService,never()).save(any(), any());
    }

    @Test
    @DisplayName("saveTown() - updates existing town when id is provided")
    void saveTown_ShouldUpdateExistingTownWhenIdIsProvided() {
        when(session.getAttribute("connectedUser")).thenReturn(adminUser);

        townController.saveTown(1L,"Paris-Updated", session);

        verify(townService).save(1L, "Paris-Updated");
    }
}
