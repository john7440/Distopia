package fr.fms.Distopia.web;

import fr.fms.Distopia.entities.Seance;
import fr.fms.Distopia.service.MovieService;
import fr.fms.Distopia.service.SeanceService;
import fr.fms.Distopia.utils.SessionUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Controller
public class SeanceController {
    @Autowired
    private SeanceService seanceService;
    @Autowired
    private MovieService movieService;

    //----------------seances d'un film----------------
    @GetMapping("/seances")
    public String seancesByMovie(@RequestParam Long movieId, Model model, Long cinemaId){
        model.addAttribute("seances", seanceService.getByMovie(movieId));
        model.addAttribute("movieId", movieId);
        model.addAttribute("cinemaId", cinemaId);
        return "seances";
    }

    //-------------page de gestion des séances------------
    @GetMapping("/admin/seances")
    public String adminSeances(@RequestParam(required = false)Long editId, Model model, HttpSession session){
        if (SessionUtils.isNotAdmin(session)) return SessionUtils.REDIRECTION;
        model.addAttribute("seances", seanceService.getAll());
        model.addAttribute("movies", movieService.getAll());
        if (editId != null) {
            seanceService.findById(editId).ifPresent(s -> model.addAttribute("editSeance", s));
        }
        return "admin-seances";
    }

    //--------------créer ou modifier une séance----------
    @PostMapping("/admin/saveSeance")
    public String saveSeance(@RequestParam(required = false) Long id, @RequestParam String dateTime,@RequestParam int availableSeats,
                             @RequestParam double price, @RequestParam Long movieId, HttpSession session){
        if (SessionUtils.isNotAdmin(session)) return SessionUtils.REDIRECTION;
        seanceService.save(id, LocalDateTime.parse(dateTime), availableSeats, price, movieId);
        return "redirect:/admin/seances";
    }

    //-------------------------supprimer une séance----------------
    @GetMapping("/admin/deleteSeance")
    public String deleteSeance(@RequestParam Long id, HttpSession session){
        if (SessionUtils.isNotAdmin(session)) return SessionUtils.REDIRECTION;
        seanceService.delete(id);
        return "redirect:/admin/seances";
    }
}
