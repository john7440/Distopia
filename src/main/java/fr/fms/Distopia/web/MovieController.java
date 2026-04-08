package fr.fms.Distopia.web;

import fr.fms.Distopia.service.CinemaService;
import fr.fms.Distopia.service.MovieService;
import fr.fms.Distopia.utils.SessionUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

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
    public String adminMovies(@RequestParam(required = false) Long editId, Model model, HttpSession session) {
        if (SessionUtils.isNotAdmin(session)) return SessionUtils.REDIRECTION;
        model.addAttribute("movies", movieService.getAll());
        model.addAttribute("cinemas", cinemaService.getAll());
        if (editId != null) {
            movieService.findById(editId).ifPresent(m -> model.addAttribute("editMovie", m));
        }
        return "admin-movies";
    }

    //----------créer ou modifier un film-----------------------------
    @PostMapping("/admin/saveMovie")
    public String saveMovie(@RequestParam(required = false) Long id, @RequestParam String title,
                            @RequestParam String description, @RequestParam int duration, @RequestParam String genre,
                            @RequestParam(required = false) List<Long> cinemaIds,
                            @RequestParam(required = false) String imageUrl, HttpSession session){
        if(SessionUtils.isNotAdmin(session)) return  SessionUtils.REDIRECTION;
        movieService.save(id, title, description, duration, genre, imageUrl,cinemaIds);
        return "redirect:/admin/movies";
    }

    //--------suppression d'un film --------------
    @GetMapping("/admin/deleteMovie")
    public String deleteMovie(@RequestParam Long id, HttpSession session){
        if(SessionUtils.isNotAdmin(session)) return  SessionUtils.REDIRECTION;
        movieService.softDelete(id);
        return "redirect:/admin/movies";
    }
}
