package fr.fms.Distopia.web;

import fr.fms.Distopia.service.MovieService;
import fr.fms.Distopia.service.SeanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SeanceController {
    @Autowired
    private SeanceService seanceService;
    @Autowired
    private MovieService movieService;

    //----------------seances d'un film----------------
    @GetMapping("/seances")
    public String seancesByMovie(@RequestParam Long movieId, Model model){
        model.addAttribute("seances", seanceService.getByMovie(movieId));
        model.addAttribute("movieId", movieId);
        return "seances";
    }
}
