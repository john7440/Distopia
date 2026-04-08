package fr.fms.Distopia.web;

import fr.fms.Distopia.dao.TownRepository;
import fr.fms.Distopia.service.TownService;
import fr.fms.Distopia.utils.SessionUtils;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TownController {

    @Autowired
    private TownService townService;

    @Autowired
    private TownRepository townRepository;

    /**
     * Displays the admin town management page
     */
    @GetMapping("/admin/towns")
    public String towns(@RequestParam(required = false) Long editId, Model model, HttpSession session) {
        if (SessionUtils.isNotAdmin(session)) return SessionUtils.REDIRECTION;
        model.addAttribute("towns", townService.getAll());
        if (editId != null) {
            townRepository.findById(editId).ifPresent(town -> model.addAttribute("editTown", town));
        }
        return "admin-towns";
    }

    //------méthode pour ajouter ou modifier une ville------------------------

    /**
     * Creates a new Town or updates an existing one
     *
     * @param id  the ID of the town to update; if null, a new town is created
     * @param name the name to assign to the town
     * @param session the current HTTP session, used to verify admin access
     * @return a redirect to /admin/towns on success, or to
     *         /index if the user is not an admin
     */
    @PostMapping("/admin/saveTown")
    public String saveTown(@RequestParam(required = false) Long id,@RequestParam String name, HttpSession session) {
        if(SessionUtils.isNotAdmin(session)) return SessionUtils.REDIRECTION;
        townService.save(id, name);
        return "redirect:/admin/towns";
    }

    //---------------méthode pour supprimer une ville----------------

    /**
     * Deletes a Town by its id
     *
     * @param id the ID of the town to delete
     * @param session the current HTTP session, used to verify admin access
     * @return a redirect to /admin/towns on success, or to
     *         /index if the user is not an admin
     */
    @Transactional
    @GetMapping("/admin/deleteTown")
    public String deleteTown(@RequestParam Long id, HttpSession session) {
        if(SessionUtils.isNotAdmin(session)) return SessionUtils.REDIRECTION;
        townService.delete(id);
        return "redirect:/admin/towns";
    }

}
