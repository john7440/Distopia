package fr.fms.Distopia.web;

import fr.fms.Distopia.service.CinemaService;
import fr.fms.Distopia.service.TownService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller responsible for handling requests to the main landing page of the application
 */
@Controller
public class IndexController {
    @Autowired
    private TownService townService;

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
        model.addAttribute("towns", townService.getAll());
        return "index";
    }

}
