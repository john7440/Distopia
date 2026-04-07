package fr.fms.Distopia.web;

import fr.fms.Distopia.service.CinemaService;
import fr.fms.Distopia.service.TownService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {
    @Autowired
    private TownService townService;

    @GetMapping({"/", "/index"})
    public String index(Model model) {
        model.addAttribute("towns", townService.getAll());
        return "index";
    }

}
