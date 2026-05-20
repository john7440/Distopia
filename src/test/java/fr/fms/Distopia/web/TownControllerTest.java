package fr.fms.Distopia.web;


import fr.fms.Distopia.dao.TownRepository;
import fr.fms.Distopia.entities.Role;
import fr.fms.Distopia.entities.Town;
import fr.fms.Distopia.entities.User;
import fr.fms.Distopia.service.TownService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private Model model;

    @InjectMocks
    private TownController townController;

    private User adminUser;
    private Town town;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setRole(Role.ADMIN);

        User regularUser = new User();
        regularUser.setId(2L);
        regularUser.setRole(Role.USER);

        town = new Town();
        town.setId(1L);
        town.setName("Dax");
    }

    @AfterEach
    void clearContext() {
        // Nettoie le SecurityContext après chaque test pour éviter les effets de bord
        SecurityContextHolder.clearContext();
    }

    private void authenticate(User user) {
        var auth = new UsernamePasswordAuthenticationToken(
                user,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    //---------------------------test for towns()--------------------
    @Test
    @DisplayName("towns() - returns 'admin-towns' view for admin user")
    void towns_ShouldReturnAdminTownsViewForAdminUser() {

        when(townService.getAll()).thenReturn(List.of(town));

        String view = townController.towns(null,model);

        assertThat(view).isEqualTo("admin-towns");
    }


    @Test
    @DisplayName("towns() - adds all towns to model")
    void towns_ShouldAddAllTownsToModel() {
        authenticate(adminUser);
        when(townService.getAll()).thenReturn(List.of(town));

        townController.towns(null,model);

        verify(model).addAttribute("towns", List.of(town));
    }

    @Test
    @DisplayName("towns() - adds editTown to model when editId is found")
    void towns_ShouldAddEitTownToModelWhenEditIdIsFound() {
        authenticate(adminUser);
        when(townService.getAll()).thenReturn(List.of(town));
        when(townRepository.findById(1L)).thenReturn(Optional.of(town));

        townController.towns(1L,model);

        verify(model).addAttribute("editTown", town);
    }

    @Test
    @DisplayName("towns() - does not add editTown to model when editId is not found")
    void towns_ShouldNotAddEitTownToModelWhenEditIdIsNotFound() {
        authenticate(adminUser);
        when(townService.getAll()).thenReturn(List.of(town));
        when(townRepository.findById(99L)).thenReturn(Optional.empty());

        townController.towns(99L,model);

        verify(model, never()).addAttribute("editTown", town);
    }

    //-----------------------tests for saveTown()-----------------------------------
    @Test
    @DisplayName("saveTown() - saves town and redirects for admin user")
    void saveTown_ShouldSaveTownAndRedirectsForAdminUser() {
        authenticate(adminUser);

        String view = townController.saveTown(null,"Paris");

        assertThat(view).isEqualTo("redirect:/admin/towns");
        verify(townService).save(null, "Paris");
    }

    @Test
    @DisplayName("saveTown() - updates existing town when id is provided")
    void saveTown_ShouldUpdateExistingTownWhenIdIsProvided() {
        authenticate(adminUser);

        townController.saveTown(1L,"Paris-Updated");

        verify(townService).save(1L, "Paris-Updated");
    }

    //---------------------------tests for deleteTown()---------------
    @Test
    @DisplayName("deleteTown() - deletes town and redirects for admin user")
    void deleteTown_ShouldDeleteTownAndRedirectsForAdminUser() {
        authenticate(adminUser);

        String view = townController.deleteTown(1L);

        assertThat(view).isEqualTo("redirect:/admin/towns");
        verify(townService).delete(1L);
    }

}
