package fr.fms.Distopia.web;

import fr.fms.Distopia.service.CinemaService;
import fr.fms.Distopia.service.MovieService;
import fr.fms.Distopia.utils.SessionUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MovieController {
    @Autowired
    private MovieService movieService;
    @Autowired
    private CinemaService cinemaService;

    //---------------films d'un cinéma--------------------
    @GetMapping("/movies")
    public String moviesByCinema(@RequestParam Long cinemaId, Model model){
        model.addAttribute("movies", movieService.getByCinema(cinemaId));
        model.addAttribute("cinemaId", cinemaId);
        return "movies";
    }

    //--------------page de gestion des films---------------------
    @GetMapping("/admin/movies")
    public String adminMovies(Model model, HttpSession session){
        if(SessionUtils.isNotAdmin(session)) return  SessionUtils.REDIRECTION;
        model.addAttribute("movies", movieService.getAll());
        model.addAttribute("cinemas", cinemaService.getAll());
        return "admin-movies";
    }
}
