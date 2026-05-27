package fr.fms.Distopia.web;

import fr.fms.Distopia.entities.*;
import fr.fms.Distopia.service.CinemaService;
import fr.fms.Distopia.service.MovieService;
import fr.fms.Distopia.service.SeanceService;
import fr.fms.Distopia.web.form.SeanceForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeanceControllerTest {
    @Mock
    private SeanceService seanceService;
    @Mock
    private MovieService movieService;
    @Mock
    private CinemaService cinemaService;
    @Mock
    private BindingResult bindingResult;
    @Mock
    private RedirectAttributes redirectAttributes;
    @Mock
    private Model model;

    @InjectMocks
    private SeanceController seanceController;

    private Seance seance;
    private Movie movie;
    private Cinema cinema;

    @BeforeEach
    void setUp() {
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setId(1L);
        adminUser.setRole(Role.ADMIN);

        User regularUser = new User();
        regularUser.setUsername("regular");
        regularUser.setId(2L);
        regularUser.setRole(Role.USER);

        movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Inception");

        cinema = new Cinema();
        cinema.setId(1L);
        cinema.setName("Cinema Test");

        seance = new Seance();
        seance.setId(1L);
        seance.setAvailableSeats(120);
        seance.setPrice(9.50);
        seance.setMovie(movie);
        seance.setCinema(cinema);
    }

    //---------------helper for seanceFOrm------------------
    private SeanceForm validSeanceForm() {
        SeanceForm form = new SeanceForm();

        form.setId(null);
        form.setDateTime(LocalDateTime.of(2026,5,26,14,0));
        form.setAvailableSeats(100);
        form.setPrice(9.50);
        form.setMovieId(1L);
        form .setCinemaId(1L);

        return form;
    }

    //----------------------------tests for seancesByMovie()-------------------------
    @Test
    @DisplayName("seancesByMovie() - return 'seances' view")
    void seancesByMovie_ShouldReturnsSeancesView() {
        when(seanceService.getByMovieAndCinema(1L,1L)).thenReturn(List.of(seance));

        String view = seanceController.seancesByMovie(1L,model, 1L);

        assertThat(view).isEqualTo("seances");
    }

    @Test
    @DisplayName("seancesByMovie() - adds seances, movieId and cinemaId to model")
    void seancesByMovie_ShouldAddSeancesAndCinemaIdToModel() {
        when(seanceService.getByMovieAndCinema(1L,1L)).thenReturn(List.of(seance));

        seanceController.seancesByMovie(1L,model,1L);

        verify(model).addAttribute("seances", List.of(seance));
        verify(model).addAttribute("movieId", 1L);
        verify(model).addAttribute("cinemaId", 1L);
    }

    @Test
    @DisplayName("seancesByMovie() - calls seanceService.getByMovieAndCinema with correct ids")
    void seancesByMovie_ShouldReturnsSeancesAndCinemaIdWithCorrectIds() {
        when(seanceService.getByMovieAndCinema(2L,3L)).thenReturn(List.of());

        seanceController.seancesByMovie(2L,model,3L);

        verify(seanceService).getByMovieAndCinema(2L,3L);
    }

    //--------------------------test for adminSeances() -----------------------------
    @Test
    @DisplayName("adminSeances() - return 'admin-seances' view for admin user")
    void adminSeances_ShouldReturnAdminSeancesViewForAdminUser() {
        Page<Seance> seancePage = new PageImpl<>(List.of(seance));
        when(seanceService.searchAdmin(null, null, 0)).thenReturn(seancePage);
        when(movieService.getAll()).thenReturn(List.of(movie));
        when(cinemaService.getAll()).thenReturn(List.of(cinema));

        String view = seanceController.adminSeances(null, null, 0, model);

        assertThat(view).isEqualTo("admin-seances");
    }


    @Test
    @DisplayName("adminSeances() - adds seances, movies and cinemas to model")
    void adminSeances_ShouldAddSeancesMoviesAndCinemasToModel() {
        Page<Seance> seancePage = new PageImpl<>(List.of(seance),
                PageRequest.of(0, 12), 1);
        when(seanceService.searchAdmin(null, null, 0)).thenReturn(seancePage);
        when(movieService.getAll()).thenReturn(List.of(movie));
        when(cinemaService.getAll()).thenReturn(List.of(cinema));

        seanceController.adminSeances(null, null, 0, model);

        verify(model).addAttribute("seancePage", seancePage);
        verify(model).addAttribute("seances", List.of(seance));
        verify(model).addAttribute("currentPage", 0);
        verify(model).addAttribute("pages", new int[1]);
    }

    @Test
    @DisplayName("adminSeances() - forwards keyword and cinemaId to service")
    void adminSeances_ShouldForwardKeywordAndCinemaIdToService() {
        Page<Seance> seancePage = new PageImpl<>(List.of(seance));
        when(seanceService.searchAdmin("14h", 1L, 0)).thenReturn(seancePage);
        when(movieService.getAll()).thenReturn(List.of(movie));
        when(cinemaService.getAll()).thenReturn(List.of(cinema));

        seanceController.adminSeances("14h", 1L, 0, model);

        verify(seanceService).searchAdmin("14h", 1L, 0);
        verify(model).addAttribute("keyword", "14h");
        verify(model).addAttribute("cinemaId", 1L);
    }

    @Test
    @DisplayName("adminSeances() - works with page > 0")
    void adminSeances_ShouldHandlePageGreaterThanZero() {
        Page<Seance> seancePage = new PageImpl<>(List.of(seance),
                PageRequest.of(2, 12), 30);
        when(seanceService.searchAdmin(null, null, 2)).thenReturn(seancePage);
        when(movieService.getAll()).thenReturn(List.of());
        when(cinemaService.getAll()).thenReturn(List.of());


        seanceController.adminSeances(null, null, 2, model);

        verify(seanceService).searchAdmin(null, null, 2);
        verify(model).addAttribute("currentPage", 2);
    }

    //----------------------tests for saveSeance()--------------------
    @Test
    @DisplayName("saveSeance() - saves seance and redirects for admin user")
    void saveSeance_ShouldSaveSeanceAndRedirectsForAdminUser() {
        SeanceForm form = validSeanceForm();

        when(bindingResult.hasErrors()).thenReturn(false);

        String view = seanceController.saveSeance(form,bindingResult,redirectAttributes);

        assertThat(view).isEqualTo("redirect:/admin/seances");
        verify(seanceService).save(null, LocalDateTime.of(2026, 5, 26, 14, 0),
                100, 9.50, 1L, 1L);
        verify(redirectAttributes, never()).addFlashAttribute(eq("error"), any());
    }

    @Test
    @DisplayName("saveSeance() - updates existing seance when id is provided")
    void saveSeance_ShouldUpdateExistingSeanceWhenIdIsProvided() {
        SeanceForm form = validSeanceForm();

        form.setId(1L);
        form.setAvailableSeats(57);
        form.setPrice(12.0);
        form.setMovieId(99L);
        form.setCinemaId(2L);

        when(bindingResult.hasErrors()).thenReturn(false);
        String view = seanceController.saveSeance(form, bindingResult,redirectAttributes);

        assertThat(view).isEqualTo("redirect:/admin/seances");
        verify(seanceService).save(1L, LocalDateTime.of(2026, 5, 26, 14, 0),
                57, 12.0, 99L, 2L);
    }

    @Test
    @DisplayName("saveSeance() - redirects with error when form is invalid")
    void saveSeance_ShouldRedirectWithError_WhenFormIsInvalid() {
        SeanceForm form = validSeanceForm();

        ObjectError error = new ObjectError("seanceForm", "error test");

        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getAllErrors()).thenReturn(List.of(error));

        String view = seanceController.saveSeance(form, bindingResult,redirectAttributes);

        assertThat(view).isEqualTo("redirect:/admin/seances");
        verify(redirectAttributes).addFlashAttribute("error", "error test");
        verify(seanceService, never()).save(any(),any(),anyInt(),anyDouble(),any(),any());
    }

    //---------------------------tests for deleteSeance()-----------------------------------
    @Test
    @DisplayName("deleteSeance() - deletes seance and redirect for admin user")
    void deleteSeance_ShouldDeleteSeanceAndRedirectsForAdminUser() {

        String view = seanceController.deleteSeance(1L);

        assertThat(view).isEqualTo("redirect:/admin/seances");
        verify(seanceService).delete(1L);
    }

}
