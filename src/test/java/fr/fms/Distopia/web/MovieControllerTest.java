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

}
