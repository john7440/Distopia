package fr.fms.Distopia.web;

import fr.fms.Distopia.entities.Movie;
import fr.fms.Distopia.entities.Seance;
import fr.fms.Distopia.service.CinemaService;
import fr.fms.Distopia.service.MovieService;
import fr.fms.Distopia.service.SeanceService;
import fr.fms.Distopia.tmdb.TmdbClient;
import fr.fms.Distopia.tmdb.dto.TmdbMovieDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controller responsible for handling movie-related web requests,
 * including both public views and administrative management
 */
@Controller
public class MovieController {
    @Autowired
    private MovieService movieService;
    @Autowired
    private CinemaService cinemaService;
    @Autowired
    private SeanceService seanceService;
    @Autowired
    private TmdbClient  tmdbClient;

    private static final String MOVIES = "movies";

    //---------------films d'un cinéma--------------------
    /**
     * Displays movies optionally filtered by cinema
     * <p>
     * Movies can also be sorted dynamically
     * @param cinemaId the selected cinema identifier
     * @param sort the sorting field
     * @param dir the sorting direction
     * @param model the Spring {@link Model} used to pass data to the view
     * @return the movies page
     */
    @GetMapping("/movies")
    public String moviesByCinema(@RequestParam(required = false) Long cinemaId,
                                 @RequestParam(defaultValue = "title") String sort,
                                 @RequestParam(defaultValue = "asc")String dir, Model model){

        Sort.Direction direction = dir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sortObj = Sort.by(direction, sort);

        if (cinemaId != null){
            model.addAttribute(MOVIES, movieService.getByCinema(cinemaId, sortObj));
        } else {
            model.addAttribute(MOVIES, movieService.getAllActive(sortObj));
        }
        model.addAttribute("cinemaId", null);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        return MOVIES;
    }

    //--------------page de gestion des films---------------------
    /**
     * Displays the movie administration page<p>
     * Supports:
     * <ul>
     *     <li>keyword search</li>
     *     <li>pagination</li>
     *     <li>sorting</li>
     *     <li>deleted movie filtering</li>
     * </ul>
     * @param keyword the movie search keyword
     * @param page the requested page number
     * @param showDeleted whether deleted movies should be displayed
     * @param sortField the selected sorting field
     * @param sortDir the sorting direction
     * @param model the Spring {@link Model} used to pass data to the view
     * @return the movie administration page
     */
    @GetMapping("/admin/movies")
    public String adminMovies(@RequestParam(required = false) String keyword,
                              @RequestParam(defaultValue = "0")   int    page,
                              @RequestParam(defaultValue = "false") boolean showDeleted,
                              @RequestParam(defaultValue = "title")    String  sortField,
                              @RequestParam(defaultValue = "asc")      String  sortDir,
                              Model model) {
        Page<Movie> moviePage = movieService.searchAdmin(keyword,showDeleted, sortField,sortDir,page);

        model.addAttribute("moviePage",moviePage);
        model.addAttribute(MOVIES,moviePage.getContent());
        model.addAttribute("pages", new int[moviePage.getTotalPages()]);
        model.addAttribute("currentPage", page);
        model.addAttribute("keyword", keyword);
        model.addAttribute("showDeleted",   showDeleted);
        model.addAttribute("sortField",     sortField);
        model.addAttribute("sortDir",       sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("cinemas",cinemaService.getAll());

        return "admin-movies";
    }

    //----------créer ou modifier un film-----------------------------
    /**
     * Creates or updates a movie
     *
     * @param id the movie identifier, or null for creation
     * @param title the movie title
     * @param description the movie description
     * @param duration the movie duration in minutes
     * @param genre the movie genre
     * @param cinemaIds the associated cinema identifiers
     * @param imageUrl the movie poster URL
     * @param trailerUrl the movie trailer URL
     * @param releaseDate the movie release date
     * @param tmdbId the TMDB movie identifier
     * @return a redirect to the movie administration page
     */
    @PostMapping("/admin/saveMovie")
    public String saveMovie(@RequestParam(required = false) Long id, @RequestParam String title,
                            @RequestParam String description, @RequestParam int duration, @RequestParam String genre,
                            @RequestParam(required = false) List<Long> cinemaIds,
                            @RequestParam(required = false) String imageUrl,@RequestParam(required = false) String trailerUrl,
                            @RequestParam(required = false)LocalDate releaseDate,
                            @RequestParam(required = false) Long tmdbId){
        movieService.save(id, tmdbId, title, description, duration, genre, imageUrl,trailerUrl,cinemaIds, releaseDate);
        return "redirect:/admin/movies";
    }

    //--------suppression d'un film --------------
    /**
     * Performs a soft deletion of a movie
     * <p>
     * <strong>Security:</strong> This endpoint is restricted to administrators
     * <p>
     * <strong>Note:</strong> Instead of physically removing the movie from the database,
     * this method delegates to {@link MovieService#softDelete(Long)}, which marks the movie
     * as deleted and disables its upcoming scheduled seances.
     *
     * @param id      the unique identifier of the movie to soft-delete
     * @return a redirection URL to the admin movies page, or the default redirection if unauthorized
     */
    @GetMapping("/admin/deleteMovie")
    public String deleteMovie(@RequestParam Long id){
        movieService.softDelete(id);
        return "redirect:/admin/movies";
    }

    /**
     * Displays the detail page of a movie<p>
     * Upcoming seances associated with the movie
     * are paginated and added to the model
     *
     * @param id the movie identifier
     * @param page the requested seance page number
     * @param model the Spring {@link Model} used to pass data to the view
     * @return the movie detail page
     */
    @GetMapping("/movie")
    public String movieDetail(@RequestParam Long id, @RequestParam(defaultValue = "0") int page, Model model){
        int size = 10;

        Movie movie = movieService.getById(id);

        Page<Seance> seancePage = seanceService.getUpcomingSeances(id, page, size);

        model.addAttribute("movie", movie);
        model.addAttribute("seancePage", seancePage);
        model.addAttribute("currentPage", page);

        return "movie-detail";
    }

    /**
     * Displays the TMDB movie detail page
     * <p>
     * If the movie already exists in the local database
     * (matched using its TMDB identifier), the user is redirected
     * to the standard movie detail page with available seances
     * <p>
     * Otherwise, the method displays a fallback TMDB-only page
     * containing movie information and trailer data
     *
     * @param tmdbId the TMDB movie identifier
     * @param model  the Spring {@link Model} used to pass data to the view
     * @return the TMDB fallback detail page, a redirect to the local movie page,
     * or the homepage if the movie cannot be found on TMDB
     */
    @GetMapping("/movie/tmdb/{tmdbId}")
    public String movieDetailTmdb(@PathVariable Long tmdbId, Model model) {
        Optional<Movie> existing =
                movieService.findByTmdbId(tmdbId);
        if (existing.isPresent()) {
            return "redirect:/movie?id="
                    + existing.get().getId();
        }
        TmdbMovieDto tmdbMovie =
                tmdbClient.getDetail(tmdbId);
        if (tmdbMovie == null) {
            return "redirect:/";
        }

        // fallback tmdb
        model.addAttribute("tmdbMovie", tmdbMovie);
        model.addAttribute("imgBase", TmdbClient.IMG_BASE);
        model.addAttribute("trailerUrl", tmdbClient.getTrailerUrl(tmdbId));

        return "movie-detail-tmdb";
    }
}
