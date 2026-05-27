package fr.fms.Distopia.web;


import fr.fms.Distopia.dao.TownRepository;
import fr.fms.Distopia.entities.Role;
import fr.fms.Distopia.entities.Town;
import fr.fms.Distopia.entities.User;
import fr.fms.Distopia.service.TownService;
import fr.fms.Distopia.web.form.TownForm;
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
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    @Mock
    private BindingResult bindingResult;
    @Mock
    private RedirectAttributes ra;

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
        TownForm form = validTownForm();

        when(bindingResult.hasErrors()).thenReturn(false);

        String view = townController.saveTown(form, bindingResult,ra);

        assertThat(view).isEqualTo("redirect:/admin/towns");
        verify(townService).save(null,"Dax");
        verify(ra, never()).addFlashAttribute(eq("error"), any());
    }

    @Test
    @DisplayName("saveTown() - updates existing town when id is provided")
    void saveTown_ShouldUpdateExistingTownWhenIdIsProvided() {
        authenticate(adminUser);
        TownForm form = validTownForm();

        form.setId(1L);
        form.setName("Saint-Geours");

        when(bindingResult.hasErrors()).thenReturn(false);

        String view = townController.saveTown(form, bindingResult,ra);
        assertThat(view).isEqualTo("redirect:/admin/towns");
        verify(townService).save(1L,"Saint-Geours");
    }

    @Test
    @DisplayName("saveTown() - redirects with error when form is invalid")
    void saveTown_ShouldRedirectWithError_WhenFormIsInvalid() {
        TownForm form = validTownForm();

        form.setName("");

        ObjectError error = new ObjectError("townForm", "error test");

        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getAllErrors()).thenReturn(List.of(error));

        String view = townController.saveTown(form, bindingResult, ra);

        assertThat(view).isEqualTo("redirect:/admin/towns");
        verify(ra).addFlashAttribute("error", "error test");
        verify(townService, never()).save(any(), any());
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

    //-------------helper valid form------
    private TownForm validTownForm() {
        TownForm form = new TownForm();

        form.setId(null);
        form.setName("Dax");

        return form;
    }
}
