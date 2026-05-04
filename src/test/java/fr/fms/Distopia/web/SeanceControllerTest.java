package fr.fms.Distopia.web;

import fr.fms.Distopia.entities.*;
import fr.fms.Distopia.service.CinemaService;
import fr.fms.Distopia.service.MovieService;
import fr.fms.Distopia.service.SeanceService;
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
class SeanceControllerTest {
    @Mock
    private SeanceService seanceService;
    @Mock
    private MovieService movieService;
    @Mock
    private CinemaService cinemaService;
    @Mock
    private Model model;
    @Mock
    private HttpSession session;

    @InjectMocks
    private SeanceController seanceController;

    private User adminUser;
    private User regularUser;
    private Seance seance;
    private Movie movie;
    private Cinema cinema;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setId(1L);
        adminUser.setRole(Role.ADMIN);

        regularUser = new User();
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
        when(session.getAttribute("connectedUser")).thenReturn(adminUser);
        when(seanceService.getAll()).thenReturn(List.of(seance));
        when(movieService.getAll()).thenReturn(List.of(movie));
        when(cinemaService.getAll()).thenReturn(List.of(cinema));

        String view = seanceController.adminSeances(null,model,session);

        assertThat(view).isEqualTo("admin-seances");
    }

    @Test
    @DisplayName("adminSeances() - return 'index' view for non-admin user")
    void adminSeances_ShouldReturnIndexViewForNonAdminUser() {
        when(session.getAttribute("connectedUser")).thenReturn(regularUser);

        String view = seanceController.adminSeances(null,model,session);

        assertThat(view).isEqualTo("redirect:/index");
    }

    @Test
    @DisplayName("adminSeances() - adds seances, movies and cinemas to model")
    void adminSeances_ShouldAddSeancesMoviesAndCinemasToModel() {
        when(session.getAttribute("connectedUser")).thenReturn(adminUser);
        when(seanceService.getAll()).thenReturn(List.of(seance));
        when(movieService.getAll()).thenReturn(List.of(movie));
        when(cinemaService.getAll()).thenReturn(List.of(cinema));

        seanceController.adminSeances(null,model,session);

        verify(model).addAttribute("seances", List.of(seance));
        verify(model).addAttribute("movies", List.of(movie));
        verify(model).addAttribute("cinemas", List.of(cinema));
    }

    @Test
    @DisplayName("adminSeances() - adds editSeance to model when editId is provided")
    void adminSeances_ShouldAddEditSeanceToModelWhenEditIdIsProvided() {
        when(session.getAttribute("connectedUser")).thenReturn(adminUser);
        when(seanceService.getAll()).thenReturn(List.of(seance));
        when(movieService.getAll()).thenReturn(List.of(movie));
        when(cinemaService.getAll()).thenReturn(List.of(cinema));
        when(seanceService.findById(1L)).thenReturn(Optional.of(seance));

        seanceController.adminSeances(1L,model,session);

        verify(model).addAttribute("editSeance", seance);
        verify(seanceService).findById(1L);
    }

    @Test
    @DisplayName("adminSeances() - does not add editSeance to model when editId is not found")
    void adminSeances_ShouldNotAddEditSeanceToModelWhenEditIdIsNotFound() {
        when(session.getAttribute("connectedUser")).thenReturn(adminUser);
        when(seanceService.getAll()).thenReturn(List.of());
        when(movieService.getAll()).thenReturn(List.of());
        when(cinemaService.getAll()).thenReturn(List.of());
        when(seanceService.findById(99L)).thenReturn(Optional.empty());

        seanceController.adminSeances(99L,model,session);

        verify(model,never()).addAttribute(eq("editSeance"), any());

    }
}
