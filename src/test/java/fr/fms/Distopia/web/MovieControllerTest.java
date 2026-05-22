package fr.fms.Distopia.web;

import fr.fms.Distopia.entities.*;
import fr.fms.Distopia.service.CinemaService;
import fr.fms.Distopia.service.MovieService;
import fr.fms.Distopia.service.SeanceService;
import fr.fms.Distopia.tmdb.TmdbClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;

import java.time.LocalDate;
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
    private TmdbClient  tmdbClient;

    @Mock
    private Model model;

    @InjectMocks
    private MovieController movieController;

    private User adminUser;
    private Movie movie;
    private Cinema cinema;
    private static final String DEFAULT_SORT = "title";
    private static final String DEFAULT_DIR  = "asc";

    @BeforeEach
    void setUp(){
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setRole(Role.ADMIN);

        User regularUser = new User();
        regularUser.setId(2L);
        regularUser.setRole(Role.USER);

        movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Inception");

        cinema = new Cinema();
        cinema.setId(1L);
        cinema.setName("Cinema Test");
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

    private Sort ascByTitle() {
        return Sort.by(Sort.Direction.ASC, "title");
    }

    //----------------------test for moviesByCinema()-----------------------------
    @Test
    @DisplayName("moviesByCinema() - return 'movies' view")
    void moviesByCinema_ShouldReturnMoviesView(){
        when(movieService.getAllActive(ascByTitle())).thenReturn(List.of(movie));

        String view = movieController.moviesByCinema(null,DEFAULT_SORT,DEFAULT_DIR, model);

        assertThat(view).isEqualTo("movies");
    }

    @Test
    @DisplayName("moviesByCinema() - loads movies by cinema when cinemaId provided")
    void moviesByCinema_ShouldLoadsMoviesByCinemaId(){
        Sort sort = Sort.by(Sort.Direction.ASC, "title");
        when(movieService.getByCinema(1L,sort)).thenReturn(List.of(movie));

        movieController.moviesByCinema(1L,DEFAULT_SORT,DEFAULT_DIR, model);

        verify(movieService).getByCinema(1L,sort);
        verify(movieService, never()).getAllActive(any(Sort.class));
        verify(model).addAttribute("movies", List.of(movie));
    }

    @Test
    @DisplayName("moviesByCinema() - loads all active movies when no cinemaID")
    void moviesByCinema_ShouldLoadsAllActiveMoviesWhenNoCinemaIdProvided(){
        Sort sort = Sort.by(Sort.Direction.ASC, "title");
        when(movieService.getAllActive(sort)).thenReturn(List.of(movie));

        movieController.moviesByCinema(null,DEFAULT_SORT,DEFAULT_DIR, model);

        verify(movieService).getAllActive(sort);
        verify(movieService, never()).getByCinema(any(),any());
    }

    @Test
    @DisplayName("moviesByCinema() - adds sort and dir to model")
    void moviesByCinema_ShouldAddSortAndDirToModel() {
        when(movieService.getAllActive(ascByTitle())).thenReturn(List.of(movie));

        movieController.moviesByCinema(null, DEFAULT_SORT, DEFAULT_DIR, model);

        verify(model).addAttribute("sort", DEFAULT_SORT);
        verify(model).addAttribute("dir", DEFAULT_DIR);
    }

    //-----------------------------tests for adminMovies()-------------------------
    @Test
    @DisplayName("adminMovies() - returns 'admin-movies' view for admin user")
    void adminMovies_ShouldReturnAdminMoviesView(){
        Page<Movie> moviePage = new PageImpl<>(List.of(movie));
        authenticate(adminUser);
        when(movieService.searchAdmin(null, false, "title", "asc", 0))
                .thenReturn(moviePage);
        when(cinemaService.getAll()).thenReturn(List.of(cinema));

        String view = movieController.adminMovies(null, 0, false, "title", "asc", model);

        assertThat(view).isEqualTo("admin-movies");
    }


    @Test
    @DisplayName("adminMovies() - adds pagination attributes to model")
    void adminMovies_ShouldAddPaginationAttributesToModel(){
        Page<Movie> moviePage = new PageImpl<>(List.of(movie),
                PageRequest.of(0, 12), 1);
        authenticate(adminUser);
        when(movieService.searchAdmin(null, false, "title", "asc", 0))
                .thenReturn(moviePage);
        when(cinemaService.getAll()).thenReturn(List.of(cinema));

        movieController.adminMovies(null, 0, false, "title", "asc", model);

        verify(model).addAttribute("moviePage", moviePage);
        verify(model).addAttribute("movies", List.of(movie));
        verify(model).addAttribute("currentPage", 0);
        verify(model).addAttribute("sortField", "title");
        verify(model).addAttribute("sortDir", "asc");
        verify(model).addAttribute("reverseSortDir", "desc");
    }

    //-----------------------------tests for saveMovie()------------------------
    @Test
    @DisplayName("saveMovie() - saves movie and redirects for admin user")
    void saveMovie_ShouldSaveMovieAndRedirectsForAdminUser(){
        authenticate(adminUser);
        LocalDate releaseDate = LocalDate.of(2025, 7, 14);

        String view = movieController.saveMovie(null,"Inception","Description",
                178, "Sci-Fi",List.of(1L), "image.url", "trailer.url", releaseDate, null);

        assertThat(view).isEqualTo("redirect:/admin/movies");
        verify(movieService).save(null,null,"Inception", "Description",178 ,"Sci-Fi","image.url",
                "trailer.url", List.of(1L),releaseDate);
    }


    //---------------------------tests for deleteMovie()--------------------------
    @Test
    @DisplayName("deleteMovie() - soft-delete movie and redirects for admin user")
    void deleteMovie_ShouldSoftDeleteMovieAndRedirectsForAdminUser(){
        authenticate(adminUser);

        String view = movieController.deleteMovie(1L);

        assertThat(view).isEqualTo("redirect:/admin/movies");
        verify(movieService).softDelete(1L);
    }


    //-----------------------tests for movieDetail()----------------------

    @Test
    @DisplayName("movieDetail() - returns 'movie-detail' view")
    void movieDetail_ShouldReturnMovieDetailView(){
        Page<Seance> seancePage = Page.empty();
        when(movieService.getById(1L)).thenReturn(movie);
        when(seanceService.getUpcomingSeances(1L, 0, 10)).thenReturn(seancePage);

        String view = movieController.movieDetail(1L,0, model);

        assertThat(view).isEqualTo("movie-detail");
    }

    @Test
    @DisplayName("movieDetail() - adds movie, seancePage and currentPage to model")
    void movieDetail_ShouldAddMovieAndUpcomingSeancesToModel(){
        Seance seance = new Seance();
        seance.setId(1L);
        Page<Seance> seancePage = new PageImpl<>(List.of(seance));

        when(movieService.getById(1L)).thenReturn(movie);
        when(seanceService.getUpcomingSeances(1L, 0, 10)).thenReturn(seancePage);

        movieController.movieDetail(1L,0,model);

        verify(model).addAttribute("movie", movie);
        verify(model).addAttribute("seancePage", seancePage);
        verify(model).addAttribute("currentPage", 0);
    }

    //---------------------------  tests for movieDetailTmdb() -----------------
    @Test
    @DisplayName("movieDetailTmdb() - redirects to local movie page when movie already exists")
    void movieDetailTmdb_ShouldRedirectToLocalMoviePageWhenMovieExists() {
        movie.setTmdbId(100L);

        when(movieService.findByTmdbId(100L)).thenReturn(Optional.of(movie));

        String view = movieController.movieDetailTmdb(100L, model);

        assertThat(view).isEqualTo("redirect:/movie?id=1");
        verify(movieService).findByTmdbId(100L);
        verify(tmdbClient, never()).getDetail(anyLong());
    }

    @Test
    @DisplayName("movieDetailTmdb() - redirects to home when tmdb movie does not exist")
    void movieDetailTmdb_ShouldRedirectHomeWhenTmdbMovieDoesNotExist() {
        when(movieService.findByTmdbId(100L)).thenReturn(Optional.empty());
        when(tmdbClient.getDetail(100L)).thenReturn(null);

        String view = movieController.movieDetailTmdb(100L, model);

        assertThat(view).isEqualTo("redirect:/");
        verify(tmdbClient).getDetail(100L);
    }
}

