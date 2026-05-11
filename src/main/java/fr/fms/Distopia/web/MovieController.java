package fr.fms.Distopia.web;

import fr.fms.Distopia.entities.Movie;
import fr.fms.Distopia.service.CinemaService;
import fr.fms.Distopia.service.MovieService;
import fr.fms.Distopia.service.SeanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

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

    private static final String MOVIES = "movies";

    //---------------films d'un cinéma--------------------
    /**
     * Handles the visitor request to display a list of movies available at a specific cinema
     *
     * @param cinemaId the unique identifier of the cinema
     * @param model    the Spring {@link Model} used to pass data to the view
     * @return the view name "movies"
     */
    @GetMapping("/movies")
    public String moviesByCinema(@RequestParam(required = false) Long cinemaId, Model model){
        if (cinemaId != null){
            model.addAttribute(MOVIES, movieService.getByCinema(cinemaId));
        } else {
            model.addAttribute(MOVIES, movieService.getAllActive());
        }
        model.addAttribute("cinemaId", null);
        return MOVIES;
    }

    //--------------page de gestion des films---------------------
    /**
     * Displays the movie management dashboard for administrators
     * <p>
     * <strong>Security:</strong> This endpoint requires the user to be logged in with
     * an "ADMIN" role. If unauthorized, the user is redirected away
     * <p>
     * If an {@code editId} is provided in the request, the corresponding movie is fetched
     * and added to the model to pre-populate the edit form on the page
     *
     * @param model   the Spring {@link Model} used to pass data to the view
     * @return the view name "admin-movies", or a redirection URL if unauthorized
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
        model.addAttribute("movies",moviePage.getContent());
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
     * Handles the creation or modification of a movie
     * <p>
     * <strong>Security:</strong> This endpoint is restricted to administrators
     * <p>
     * This method processes the form submission to save a movie along with its
     * associated cinemas. Upon successful completion, it redirects the user back
     * to the movie management page
     *
     * @param id          the unique identifier of the movie to update (null for creation)
     * @param title       the title of the movie
     * @param description the synopsis or description of the movie
     * @param duration    the duration of the movie in minutes
     * @param genre       the genre of the movie (Action, Sci-Fi)
     * @param cinemaIds   a list of cinema identifiers where the movie will be screened (optional)
     * @param imageUrl    the URL pointing to the movie's poster or cover image (optional)
     * @return a redirection URL to the admin movies page, or the default redirection if unauthorized
     */
    @PostMapping("/admin/saveMovie")
    public String saveMovie(@RequestParam(required = false) Long id, @RequestParam String title,
                            @RequestParam String description, @RequestParam int duration, @RequestParam String genre,
                            @RequestParam(required = false) List<Long> cinemaIds,
                            @RequestParam(required = false) String imageUrl,@RequestParam(required = false) String trailerUrl,
                            @RequestParam(required = false)LocalDate releaseDate){
        movieService.save(id, title, description, duration, genre, imageUrl,trailerUrl,cinemaIds, releaseDate);
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

    @GetMapping("/movie")
    public String movieDetail(@RequestParam Long id, Model model){
        movieService.findById(id).ifPresent(m -> model.addAttribute("movie", m));
        model.addAttribute("seances", seanceService.getUpcomingByMovie(id));
        return "movie-detail";
    }
}
