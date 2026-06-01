package fr.fms.Distopia.web;

import fr.fms.Distopia.dao.TownRepository;
import fr.fms.Distopia.service.TownService;
import fr.fms.Distopia.web.form.TownForm;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller responsible for town administration pages.
 * <p>
 * It displays towns and handles creation, edition and deletion actions.
 */
@Controller
public class TownController {

    private final TownService townService;
    private final TownRepository townRepository;

    public TownController(TownService townService, TownRepository townRepository) {
        this.townService = townService;
        this.townRepository = townRepository;
    }

    private static final String REDIRECT_ADMIN_TOWNS ="redirect:/admin/towns";

    /**
     * Displays the town administration page<p>
     * If an edit identifier is provided,
     * the corresponding town is loaded and added to the model
     * for edition
     *
     * @param editId the identifier of the town being edited
     * @param model the Spring {@link Model} used to pass data to the view
     * @return the town administration page
     */
    @GetMapping("/admin/towns")
    public String towns(@RequestParam(required = false) Long editId, Model model) {
        model.addAttribute("towns", townService.getAll());
        if (editId != null) {
            townRepository.findById(editId).ifPresent(town -> model.addAttribute("editTown", town));
        }
        return "admin-towns";
    }

    //------méthode pour ajouter ou modifier une ville------------------------
    /**
     * Creates or updates a town<p>
     * The submitted town form is validated before saving.
     * If validation fails, the user is redirected back to the town administration page
     * with an error message stored in flash attributes
     *
     * @param form the validated town form containing town data
     * @param bindingResult the validation result for the submitted form
     * @param ra the Spring {@link RedirectAttributes} used to pass flash messages
     * @return a redirect to the town administration page
     */
    @PostMapping("/admin/saveTown")
    public String saveTown(@Valid @ModelAttribute TownForm form, BindingResult bindingResult, RedirectAttributes ra) {
        if(bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().isEmpty()
                    ? "Données invalides"
                    : bindingResult.getAllErrors().get(0).getDefaultMessage();
            ra.addFlashAttribute("error", errorMessage);

            return REDIRECT_ADMIN_TOWNS;
        }

        townService.save(form.getId(), form.getName());
        return REDIRECT_ADMIN_TOWNS;
    }

    //---------------méthode pour supprimer une ville----------------
    /**
     * Deletes a Town by its id
     *
     * @param id the ID of the town to delete
     * @return a redirect to the town administration page
     */
    @Transactional
    @PostMapping("/admin/deleteTown")
    public String deleteTown(@RequestParam Long id) {
        townService.delete(id);
        return REDIRECT_ADMIN_TOWNS;
    }

}
