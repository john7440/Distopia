package fr.fms.Distopia.web;

import fr.fms.Distopia.service.CinemaService;
import fr.fms.Distopia.service.TownService;
import fr.fms.Distopia.tmdb.TmdbClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * Controller responsible for handling requests to the main landing page of the application
 */
@Controller
public class IndexController {
    @Autowired
    private TmdbClient tmdbClient;
    /**
     * Displays the home page of the application
     * <p>
     * This method maps to both the root URL ("/") and the "/index" path.
     * It fetches all available towns from the database and adds them to the
     * model, which allows the view to display them
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
        } catch (Exception e) {
            System.err.println("Erreur TMDB index : " + e.getMessage());
            model.addAttribute("nowPlaying", List.of());
            model.addAttribute("thisWeek",   List.of());
            model.addAttribute("upcoming",   List.of());
        }
        return "index";
    }

}
