package fr.fms.Distopia.web;

import fr.fms.Distopia.entities.Cinema;
import fr.fms.Distopia.entities.Movie;
import fr.fms.Distopia.entities.Role;
import fr.fms.Distopia.entities.User;
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
class MovieControllerTest {
    @Mock
    private MovieService movieService;

    @Mock
    private CinemaService cinemaService;

    @Mock
    private SeanceService seanceService;

    @Mock
    private Model model;

    @Mock
    private HttpSession session;

    @InjectMocks
    private MovieController movieController;

    private User adminUser;
    private User regularUser;
    private Movie movie;
    private Cinema cinema;

    @BeforeEach
    void setUp(){
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setRole(Role.ADMIN);

        regularUser = new User();
        regularUser.setId(2L);
        regularUser.setRole(Role.USER);

        movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Inception");

        cinema = new Cinema();
        cinema.setId(1L);
        cinema.setName("Cinema Test");
    }

    //----------------------test for moviesByCinema()-----------------------------
    @Test
    @DisplayName("moviesByCinema() - return 'movies' view")
    void moviesByCinema_ShouldReturnMoviesView(){
        when(movieService.getAllActive()).thenReturn(List.of(movie));

        String view = movieController.moviesByCinema(null, model);

        assertThat(view).isEqualTo("movies");
    }

    @Test
    @DisplayName("moviesByCinema() - loads movies by cinema when cinemaId provided")
    void moviesByCinema_ShouldLoadsMoviesByCinemaId(){
        when(movieService.getByCinema(1L)).thenReturn(List.of(movie));

        movieController.moviesByCinema(1L, model);

        verify(movieService).getByCinema(1L);
        verify(movieService, never()).getAllActive();
        verify(model).addAttribute("movies", List.of(movie));
    }

    @Test
    @DisplayName("moviesByCinema() - loads all active movies when no cinemaID")
    void moviesByCinema_ShouldLoadsAllActiveMoviesWhenNoCinemaIdProvided(){
        when(movieService.getAllActive()).thenReturn(List.of(movie));

        movieController.moviesByCinema(null, model);

        verify(movieService).getAllActive();
        verify(movieService, never()).getByCinema(any());
    }

    //-----------------------------tests for adminMovies()-------------------------
    @Test
    @DisplayName("adminMovies() - returns 'admin-movies' view for admin user")
    void adminMovies_ShouldReturnAdminMoviesView(){
        when(session.getAttribute("connectedUser")).thenReturn(adminUser);
        when(movieService.getAll()).thenReturn(List.of(movie));
        when(cinemaService.getAll()).thenReturn(List.of(cinema));

        String view = movieController.adminMovies(null, model, session);

        assertThat(view).isEqualTo("admin-movies");
    }

    @Test
    @DisplayName("adminMovies() - should redirects non-admin user")
    void adminMovies_ShouldRedirectNonAdminUser(){
        when(session.getAttribute("connectedUser")).thenReturn(regularUser);

        String view = movieController.adminMovies(null, model, session);

        assertThat(view).isEqualTo("redirect:/index");
        verify(movieService, never()).getAll();
    }

    @Test
    @DisplayName("adminMovies() - adds editMovie to model when editId is provided")
    void adminMovies_ShouldAddEditMovieToModelWhenEditIdIsProvided(){
        when(session.getAttribute("connectedUser")).thenReturn(adminUser);
        when(movieService.getAll()).thenReturn(List.of(movie));
        when(cinemaService.getAll()).thenReturn(List.of(cinema));
        when(movieService.findById(1L)).thenReturn(Optional.of(movie));

        movieController.adminMovies(1L, model, session);

        verify(movieService).findById(1L);
        verify(model).addAttribute("editMovie", movie);
    }
}
