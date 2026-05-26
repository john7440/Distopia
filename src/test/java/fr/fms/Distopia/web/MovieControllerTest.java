package fr.fms.Distopia.web;

import fr.fms.Distopia.entities.*;
import fr.fms.Distopia.service.CinemaService;
import fr.fms.Distopia.service.MovieService;
import fr.fms.Distopia.service.SeanceService;
import fr.fms.Distopia.tmdb.TmdbClient;
import fr.fms.Distopia.tmdb.dto.TmdbMovieDto;
import fr.fms.Distopia.web.form.MovieForm;
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
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    @Mock
    private BindingResult bindingResult;
    @Mock
    private RedirectAttributes redirectAttributes;

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

    /**
     * Create a valid Movie Form for test
     * @return the form
     */
    private MovieForm validMovieForm() {
        MovieForm form = new MovieForm();

        form.setId(null);
        form.setTmdbId(null);
        form.setTitle("Inception");
        form.setDescription("Description");
        form.setDuration(178);
        form.setGenre("Sci-Fi");
        form.setCinemaIds(List.of(1L));
        form.setImageUrl("image.url");
        form.setTrailerUrl("trailer.url");
        form.setReleaseDate(LocalDate.of(2025, 7, 14));

        return form;
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
        MovieForm form = validMovieForm();

        when(bindingResult.hasErrors()).thenReturn(false);

        String view = movieController.saveMovie(form,bindingResult, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/admin/movies");
        verify(movieService).save(null,null,"Inception", "Description", 178,
                "Sci-Fi", "image.url", "trailer.url", List.of(1L), LocalDate.of(2025,7,14));
        verify(redirectAttributes, never()).addFlashAttribute(eq("error"), any());
    }

    @Test
    @DisplayName("saveMovie() - redirects with error when form is invalid")
    void saveMovie_ShouldRedirectWithError_WhenFormIsInvalid() {
        authenticate(adminUser);
        MovieForm form = validMovieForm();
        ObjectError error = new ObjectError("movieForm", "Le titre est obligatoire");

        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getAllErrors()).thenReturn(List.of(error));

        String view = movieController.saveMovie(form, bindingResult, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/admin/movies");
        verify(redirectAttributes).addFlashAttribute("error", "Le titre est obligatoire");
        verify(movieService, never()).save(any(), any(), any(), any(), anyInt(), any(),
                any(), any(), any(), any());
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

    @Test
    @DisplayName("movieDetailTmdb() - adds tmdb movie data to model when movie does not exist locally")
    void movieDetailTmdb_ShouldAddTmdbMovieDataToModelWhenMovieDoesNotExistLocally() {
        TmdbMovieDto tmdbMovie = new TmdbMovieDto();
        tmdbMovie.setId(100L);
        tmdbMovie.setTitle("Film tmbd");

        when(movieService.findByTmdbId(100L)).thenReturn(Optional.empty());
        when(tmdbClient.getDetail(100L)).thenReturn(tmdbMovie);
        when(tmdbClient.getTrailerUrl(100L)).thenReturn("https://youtube.com/trailer");

        String view = movieController.movieDetailTmdb(100L, model);

        assertThat(view).isEqualTo("movie-detail-tmdb");
        verify(model).addAttribute("tmdbMovie", tmdbMovie);
        verify(model).addAttribute("imgBase", TmdbClient.IMG_BASE);
        verify(model).addAttribute("trailerUrl", "https://youtube.com/trailer");
    }

    @Test
    @DisplayName("movieDetailTmdb() - loads trailer url from tmdb client")
    void movieDetailTmdb_ShouldLoadTrailerUrlFromTmdbClient() {

        TmdbMovieDto tmdbMovie = new TmdbMovieDto();

        when(movieService.findByTmdbId(100L)).thenReturn(Optional.empty());
        when(tmdbClient.getDetail(100L)).thenReturn(tmdbMovie);
        when(tmdbClient.getTrailerUrl(100L)).thenReturn("https://youtube.com/embed/test");

        movieController.movieDetailTmdb(100L, model);

        verify(tmdbClient).getTrailerUrl(100L);
    }
}

