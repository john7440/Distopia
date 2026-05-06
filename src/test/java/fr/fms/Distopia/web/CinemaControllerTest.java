package fr.fms.Distopia.web;

import fr.fms.Distopia.entities.Cinema;
import fr.fms.Distopia.entities.Role;
import fr.fms.Distopia.entities.Town;
import fr.fms.Distopia.entities.User;
import fr.fms.Distopia.service.CinemaService;
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
class CinemaControllerTest {
    @Mock
    private CinemaService cinemaService;
    @Mock
    private TownService townService;
    @Mock
    private Model model;
    @InjectMocks
    private CinemaController cinemaController;

    private User adminUser;
    private User regularUser;
    private Cinema cinema;
    private Town town;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUsername("admin");
        adminUser.setRole(Role.ADMIN);

        regularUser = new User();
        regularUser.setId(2L);
        regularUser.setUsername("user");
        regularUser.setRole(Role.USER);

        town = new Town();
        town.setId(1L);
        town.setName("Paris");

        cinema = new Cinema();
        cinema.setId(1L);
        cinema.setName("Cinema Test");
        cinema.setAddress("57 rue du Test");
        cinema.setTown(town);
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

    //---------------------tests for cinemasByTown()--------------------------

    @Test
    @DisplayName("cinemasByTown() - return 'cinemas' view")
    void cinemasByTown_ShouldReturnCinemasView() {
        when(townService.getAll()).thenReturn(List.of(town));
        when(cinemaService.search(null,null)).thenReturn(List.of(cinema));

        String view = cinemaController.cinemasByTown(null,null,model);

        assertThat(view).isEqualTo("cinemas");
    }

    @Test
    @DisplayName("cinemasByTown() - adds filtered cinemas and towns to model")
    void cinemasByTown_ShouldAddCinemasAndTownToModel(){
        when(townService.getAll()).thenReturn(List.of(town));
        when(cinemaService.search("Test",1L)).thenReturn(List.of(cinema));

        cinemaController.cinemasByTown(1L,"Test", model);

        verify(model).addAttribute("towns", List.of(town));
        verify(model).addAttribute("cinemas", List.of(cinema));
        verify(model).addAttribute("selectedTownId", 1L);
        verify(model).addAttribute("keyword", "Test");
    }

    @Test
    @DisplayName("cinemasByTown() - uses empty string for keyword when null")
    void cinemasByTown_ShouldUsesEmptyStringForKeywordWhenNull(){
        when(townService.getAll()).thenReturn(List.of());
        when(cinemaService.search(null,null)).thenReturn(List.of());

        cinemaController.cinemasByTown(null,null,model);

        verify(model).addAttribute("keyword", "");
    }

    //--------------------tests for adminCinemas()------------------
    @Test
    @DisplayName("adminCinemas() - return 'admin-cinemas' view for admin user")
    void adminCinemas_ShouldReturnAdminCinemasView() {
        authenticate(adminUser);
        when(cinemaService.getAll()).thenReturn(List.of(cinema));
        when(townService.getAll()).thenReturn(List.of(town));

        String view = cinemaController.adminCinemas(null,model);

        assertThat(view).isEqualTo("admin-cinemas");
    }


    @Test
    @DisplayName("adminCinemas() - adds editCinema to model when editId is provided")
    void adminCinemas_shouldAddEditCinemaToModel_whenEditIdProvided() {
        authenticate(adminUser);
        when(cinemaService.getAll()).thenReturn(List.of(cinema));
        when(townService.getAll()).thenReturn(List.of(town));
        when(cinemaService.findById(1L)).thenReturn(Optional.of(cinema));

        cinemaController.adminCinemas(1L, model);

        verify(cinemaService).findById(1L);
        verify(model).addAttribute("editCinema", cinema);
    }


    //--------------------tests for saveCinema()--------------------------------
    @Test
    @DisplayName("saveCinema() - saves cinemas and redirects to admin page for admin user")
    void saveCinema_ShouldSaveAndRedirectToAdminUserPage() {
        authenticate(adminUser);

        String view = cinemaController.saveCinema(null, "Test", "Adresse", 1L);

        assertThat(view).isEqualTo("redirect:/admin/cinemas");
        verify(cinemaService).save(null,"Test","Adresse",1L);
    }


    //----------------tests for deleteCinema()------------------------------------

    @Test
    @DisplayName("deleteCinema() - deletes cinemas and redirects for admin user")
    void deleteCinema_ShouldDeleteCinemasAndRedirectsForAdminUser() {
        authenticate(adminUser);

        String view = cinemaController.deleteCinema(1L);

        assertThat(view).isEqualTo("redirect:/admin/cinemas");
        verify(cinemaService).delete(1L);
    }

}
