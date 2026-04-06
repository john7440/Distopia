package fr.fms.Distopia.web;

import fr.fms.Distopia.service.CinemaService;
import fr.fms.Distopia.service.TownService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CinemaController {

    @Autowired
    private CinemaService cinemaService;

    @Autowired
    private TownService townService;

    //---------pour visiteur — cinémas d'une ville-----------------
    @GetMapping("/cinemas")
    public String cinemasByTown(@RequestParam(required = false)Long townId, Model model){
        model.addAttribute("towns", townService.getAll());
        if(townId != null){
            model.addAttribute("cinemas", cinemaService.getByTown(townId));
            model.addAttribute("selectedTownId", townId);
        }
        return "cinemas";
    }




}
