package fr.fms.Distopia.web;

import fr.fms.Distopia.tmdb.TmdbClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * Controller responsible for handling requests to the main landing page of the application
 */
@Controller
public class IndexController {

    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);

    private final TmdbClient tmdbClient;

    public IndexController(TmdbClient tmdbClient){
        this.tmdbClient = tmdbClient;
    }

    /**
     * Displays the home page of the application
     * <p>
     * This method maps to both the root URL ("/") and the "/index" path.
     * It loads movies from TMDB for the homepage sections:
     * currently playing movies, movies released this week and upcoming movies
     * <p>
     * If the TMDB API cannot be reached or returns an error,
     * empty lists are added to the model to keep the homepage available
     *
     * @param model the Spring {@link Model} used to pass data to the view
     * @return the view name "index"
     */
    @GetMapping({"/", "/index"})
    public String index(Model model) {
        model.addAttribute("imgBase", TmdbClient.IMG_BASE);
        try {
            model.addAttribute("nowPlaying", tmdbClient.getNowPlaying().stream().limit(10).toList());
            model.addAttribute("thisWeek",tmdbClient.getThisWeek());
            model.addAttribute("upcoming", tmdbClient.getUpcoming().stream().limit(10).toList());
            logger.info("TMDB homepage movies loaded successfully");
        } catch (Exception e) {
            logger.error("Failed to load TMDB homepage movies", e);
            model.addAttribute("nowPlaying", List.of());
            model.addAttribute("thisWeek",   List.of());
            model.addAttribute("upcoming",   List.of());
        }
        return "index";
    }
}
