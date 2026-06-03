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
        when(seanceService.searchAdmin(null, null,true,"sortField","sortDir", 0)).thenReturn(seancePage);
        when(movieService.getAll()).thenReturn(List.of(movie));
        when(cinemaService.getAll()).thenReturn(List.of(cinema));

        String view = seanceController.adminSeances(null, null, true,0,"sortField","sortDir", model);

        assertThat(view).isEqualTo("admin-seances");
    }


    @Test
    @DisplayName("adminSeances() - adds seances, movies and cinemas to model")
    void adminSeances_ShouldAddSeancesMoviesAndCinemasToModel() {
        Page<Seance> seancePage = new PageImpl<>(List.of(seance),
                PageRequest.of(0, 12), 1);
        when(seanceService.searchAdmin(null, null,true,"sortField","sortDir", 0)).thenReturn(seancePage);
        when(movieService.getAll()).thenReturn(List.of(movie));
        when(cinemaService.getAll()).thenReturn(List.of(cinema));

        seanceController.adminSeances(null, null, true,0,"sortField","sortDir", model);

        verify(model).addAttribute("seancePage", seancePage);
        verify(model).addAttribute("seances", List.of(seance));
        verify(model).addAttribute("currentPage", 0);
        verify(model).addAttribute("pages", new int[1]);
    }

    @Test
    @DisplayName("adminSeances() - forwards keyword and cinemaId to service")
    void adminSeances_ShouldForwardKeywordAndCinemaIdToService() {
        Page<Seance> seancePage = new PageImpl<>(List.of(seance));
        when(seanceService.searchAdmin("14h", 1L,true,"sortField","sortDir",0)).thenReturn(seancePage);
        when(movieService.getAll()).thenReturn(List.of(movie));
        when(cinemaService.getAll()).thenReturn(List.of(cinema));

        seanceController.adminSeances("14h", 1L, true,0,"sortField","sortDir", model);

        verify(seanceService).searchAdmin("14h", 1L,true,"sortField", "sortDir", 0);
        verify(model).addAttribute("keyword", "14h");
        verify(model).addAttribute("cinemaId", 1L);
    }

    @Test
    @DisplayName("adminSeances() - works with page > 0")
    void adminSeances_ShouldHandlePageGreaterThanZero() {
        Page<Seance> seancePage = new PageImpl<>(List.of(seance),
                PageRequest.of(2, 12), 30);
        when(seanceService.searchAdmin(null, null,true,"sortField","sortDir",2)).thenReturn(seancePage);
        when(movieService.getAll()).thenReturn(List.of());
        when(cinemaService.getAll()).thenReturn(List.of());


        seanceController.adminSeances(null, null, true,2,"sortField","sortDir", model);

        verify(seanceService).searchAdmin(null, null,true,"sortField","sortDir", 2);
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
    @DisplayName("deleteSeance() - deletes seance and redirects with success message")
    void deleteSeance_ShouldDeleteSeanceAndRedirectWithSuccessMessage() {
        String view = seanceController.deleteSeance(1L, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/admin/seances");
        verify(seanceService).delete(1L);
        verify(redirectAttributes).addFlashAttribute("message", "Séance supprimée avec succès");
        verify(redirectAttributes, never()).addFlashAttribute(eq("error"), any());
    }

    @Test
    @DisplayName("deleteSeance() - redirects with error when seance has reservations")
    void deleteSeance_ShouldRedirectWithErrorWhenSeanceHasReservations() {
        doThrow(new IllegalStateException("Impossible de supprimer une séance avec des réservations"))
                .when(seanceService).delete(1L);

        String view = seanceController.deleteSeance(1L, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/admin/seances");
        verify(seanceService).delete(1L);
        verify(redirectAttributes).addFlashAttribute("error",
                "Impossible de supprimer une séance avec des réservations");
        verify(redirectAttributes, never()).addFlashAttribute(eq("message"), any());
    }

    //---------------------------tests for deleteFilteredSeances()-----------------------------------
    @Test
    @DisplayName("deleteFilteredSeances() - redirects with warning when no seance matches filters")
    void deleteFilteredSeances_ShouldRedirectWithWarningWhenNoSeanceMatchesFilters() {
        when(seanceService.deleteByAdminFilters("avatar", 5L,true)).thenReturn(0);

        String view = seanceController.deleteFilteredSeances("avatar", 5L, true,null, null,
                redirectAttributes);

        assertThat(view).isEqualTo("redirect:/admin/seances?keyword=avatar&cinemaId=5&showArchived=true");
        verify(seanceService).deleteByAdminFilters("avatar", 5L,true);
        verify(redirectAttributes).addFlashAttribute("warning", "Aucune séance ne correspond aux filtres actuels");
        verify(redirectAttributes, never()).addFlashAttribute(eq("message"), any());
        verify(redirectAttributes, never()).addFlashAttribute(eq("error"), any());
    }

    @Test
    @DisplayName("deleteFilteredSeances() - deletes filtered seances and redirects with success message")
    void deleteFilteredSeances_ShouldDeleteFilteredSeancesAndRedirectWithSuccessMessage() {
        when(seanceService.deleteByAdminFilters("avatar", 5L, true)).thenReturn(4);

        String view = seanceController.deleteFilteredSeances("avatar", 5L, true,"dateTime", "desc",
                redirectAttributes);

        assertThat(view).isEqualTo("redirect:/admin/seances?keyword=avatar&cinemaId=5&showArchived=true&sortField=dateTime&sortDir=desc");
        verify(seanceService).deleteByAdminFilters("avatar", 5L, true);
        verify(redirectAttributes).addFlashAttribute("message", "4 séance(s) supprimée(s) selon les filtres actuels");
        verify(redirectAttributes, never()).addFlashAttribute(eq("warning"), any());
        verify(redirectAttributes, never()).addFlashAttribute(eq("error"), any());
    }

    @Test
    @DisplayName("deleteFilteredSeances() - redirects with error when filtered seances have reservations")
    void deleteFilteredSeances_ShouldRedirectWithErrorWhenFilteredSeancesHaveReservations() {
        doThrow(new IllegalStateException("Impossible de supprimer une ou plusieurs séances car elles possèdent déjà des réservations"))
                .when(seanceService).deleteByAdminFilters("avatar", 5L,true);

        String view = seanceController.deleteFilteredSeances("avatar", 5L, true,"dateTime",
                "asc", redirectAttributes);

        assertThat(view).isEqualTo("redirect:/admin/seances?keyword=avatar&cinemaId=5&showArchived=true&sortField=dateTime&sortDir=asc");
        verify(seanceService).deleteByAdminFilters("avatar", 5L,true);
        verify(redirectAttributes).addFlashAttribute(
                "error",
                "Impossible de supprimer une ou plusieurs séances car elles possèdent déjà des réservations"
        );
        verify(redirectAttributes, never()).addFlashAttribute(eq("message"), any());
        verify(redirectAttributes, never()).addFlashAttribute(eq("warning"), any());
    }

    @Test
    @DisplayName("deleteFilteredSeances() - redirects without empty filters")
    void deleteFilteredSeances_ShouldRedirectWithoutEmptyFilters() {
        when(seanceService.deleteByAdminFilters("", null, true)).thenReturn(0);

        String view = seanceController.deleteFilteredSeances("", null, true,"",
                "", redirectAttributes);

        assertThat(view).isEqualTo("redirect:/admin/seances?showArchived=true");
        verify(seanceService).deleteByAdminFilters("", null, true);
        verify(redirectAttributes).addFlashAttribute("warning", "Aucune séance ne correspond aux filtres actuels");
    }

    //---------------------------tests for deleteSelectedSeances()-----------------------------------
    @Test
    @DisplayName("deleteSelectedSeances() - redirects with warning when no seance is selected")
    void deleteSelectedSeances_ShouldRedirectWithWarning_WhenNoSeanceIsSelected() {
        when(seanceService.deleteSelected(null)).thenReturn(0);

        String view = seanceController.deleteSelectedSeances(null, null, null, false,
                null, null, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/admin/seances");

        verify(seanceService).deleteSelected(null);
        verify(redirectAttributes).addFlashAttribute("warning", "Aucune séance sélectionnée!");
        verify(redirectAttributes, never()).addFlashAttribute(eq("message"), any());
        verify(redirectAttributes, never()).addFlashAttribute(eq("error"), any());
    }

    @Test
    @DisplayName("deleteSelectedSeances() - deletes selected seances and redirects with success message")
    void deleteSelectedSeances_ShouldDeleteSelectedSeancesAndRedirectWithSuccessMessage() {
        List<Long> selectedIds = List.of(1L, 2L, 3L);

        when(seanceService.deleteSelected(selectedIds)).thenReturn(3);

        String view = seanceController.deleteSelectedSeances(selectedIds, null, null,
                false, null, null, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/admin/seances");

        verify(seanceService).deleteSelected(selectedIds);
        verify(redirectAttributes).addFlashAttribute("message", "3 séance(s) supprimée(s).");
        verify(redirectAttributes, never()).addFlashAttribute(eq("warning"), any());
        verify(redirectAttributes, never()).addFlashAttribute(eq("error"), any());
    }

    @Test
    @DisplayName("deleteSelectedSeances() - preserves filters after deleting selected seances")
    void deleteSelectedSeances_ShouldPreserveFiltersAfterDeletingSelectedSeances() {
        List<Long> selectedIds = List.of(1L, 2L);

        when(seanceService.deleteSelected(selectedIds)).thenReturn(2);

        String view = seanceController.deleteSelectedSeances(selectedIds, "avatar", 5L,
                true, "dateTime", "asc", redirectAttributes);

        assertThat(view).isEqualTo("redirect:/admin/seances?keyword=avatar&cinemaId=5&showArchived=" +
                "true&sortField=dateTime&sortDir=asc");

        verify(seanceService).deleteSelected(selectedIds);
        verify(redirectAttributes).addFlashAttribute("message", "2 séance(s) supprimée(s).");
    }

    @Test
    @DisplayName("deleteSelectedSeances() - redirects with error when selected seances have reservations")
    void deleteSelectedSeances_ShouldRedirectWithError_WhenSelectedSeancesHaveReservations() {
        List<Long> selectedIds = List.of(1L, 2L);

        doThrow(new IllegalStateException("Impossible de supprimer une ou plusieurs séances car elles possèdent déjà des réservations"))
                .when(seanceService).deleteSelected(selectedIds);

        String view = seanceController.deleteSelectedSeances(selectedIds, "avatar", 5L,
                true, "dateTime", "asc", redirectAttributes);

        assertThat(view).isEqualTo("redirect:/admin/seances?keyword=avatar&cinemaId=5&showArchived=" +
                "true&sortField=dateTime&sortDir=asc");

        verify(seanceService).deleteSelected(selectedIds);
        verify(redirectAttributes).addFlashAttribute("error",
                "Impossible de supprimer une ou plusieurs séances car elles possèdent déjà des réservations");
        verify(redirectAttributes, never()).addFlashAttribute(eq("message"), any());
        verify(redirectAttributes, never()).addFlashAttribute(eq("warning"), any());
    }
}
