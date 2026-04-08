package fr.fms.Distopia.web;

import fr.fms.Distopia.service.CinemaService;
import fr.fms.Distopia.service.TownService;
import fr.fms.Distopia.utils.SessionUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CinemaController {

    @Autowired
    private CinemaService cinemaService;

    @Autowired
    private TownService townService;

    //---------pour visiteur — cinémas d'une ville-----------------
    @GetMapping("/cinemas")
    public String cinemasByTown(@RequestParam(required = false)Long townId,
                                @RequestParam(required = false) String keyword, Model model){
        model.addAttribute("towns", townService.getAll());
        model.addAttribute("cinemas", cinemaService.search(keyword, townId));
        model.addAttribute("selectedTownId", townId);
        model.addAttribute("keyword", keyword != null ? keyword : "");
        return "cinemas";
    }

    //------------pour admin - page de gestion des cinémas-------------
    @GetMapping("/admin/cinemas")
    public String adminCinemas(@RequestParam(required = false) Long editId,
                               Model model, HttpSession session) {
        if (SessionUtils.isNotAdmin(session)) return SessionUtils.REDIRECTION;
        model.addAttribute("cinemas", cinemaService.getAll());
        model.addAttribute("towns", townService.getAll());
        if (editId != null) {
            cinemaService.findById(editId).ifPresent(c -> model.addAttribute("editCinema", c));
        }
        return "admin-cinemas";
    }

    //--------------créer ou modifier un cinééma -----------------------
    @PostMapping("/admin/saveCinema")
    public String saveCinema(@RequestParam(required = false) Long id, @RequestParam String name,
                             @RequestParam String address, @RequestParam(required = false) Long townId, HttpSession session){
        if(SessionUtils.isNotAdmin(session)) return SessionUtils.REDIRECTION;
        cinemaService.save(id, name, address, townId);
        return "redirect:/admin/cinemas";
    }

    @GetMapping("/admin/deleteCinema")
    public String deleteCinema(@RequestParam Long id, HttpSession session){
        if(SessionUtils.isNotAdmin(session)) return SessionUtils.REDIRECTION;
        cinemaService.delete(id);
        return "redirect:/admin/cinemas";
    }



}
