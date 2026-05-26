package fr.fms.Distopia.web;

import fr.fms.Distopia.entities.Cinema;
import fr.fms.Distopia.service.CinemaCsvImporter;
import fr.fms.Distopia.service.CinemaService;
import fr.fms.Distopia.service.TownService;

import fr.fms.Distopia.web.form.CinemaForm;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller responsible for handling cinema-related web requests,
 * including both public views and administrative management
 */
@Controller
public class CinemaController {

    private final CinemaService cinemaService;
    private final TownService townService;
    private final CinemaCsvImporter cinemaCsvImporter;

    public CinemaController(CinemaService cinemaService, TownService townService, CinemaCsvImporter cinemaCsvImporter) {
        this.cinemaService = cinemaService;
        this.townService = townService;
        this.cinemaCsvImporter = cinemaCsvImporter;
    }

    private static final String CINEMAS = "cinemas";
    private static final String ADMIN_REDIRECT = "redirect:/admin/cinemas";

    //---------pour visiteur — cinémas d'une ville-----------------
    /**
     * Displays public cinemas with optional filters<p>
     * Supported filters:
     * <ul>
     *     <li>town</li>
     *     <li>keyword</li>
     *     <li>department</li>
     * </ul><p>
     * Note: results are paginated
     *
     * @param townId the selected town identifier
     * @param keyword the keyword used for cinema search
     * @param department the selected department code
     * @param page the requested page number
     * @param model the Spring {@link Model} used to pass data to the view
     * @return the public cinemas page
     */
    @GetMapping("/cinemas")
    public String cinemasByTown(@RequestParam(required = false)Long townId,
                                @RequestParam(required = false) String keyword,
                                @RequestParam(required = false) String department,
                                @RequestParam(defaultValue = "0") int page,Model model){

        Page<Cinema> cinemaPage = cinemaService.searchPublic(keyword, townId,department, page);
        List<String> departments = cinemaService.getAllDepartments();

        model.addAttribute("towns", townService.getAll());
        model.addAttribute(CINEMAS,cinemaPage.getContent());
        model.addAttribute("cinemaPage", cinemaPage);
        model.addAttribute("pages", new int[cinemaPage.getTotalPages()]);
        model.addAttribute("currentPage", page);
        model.addAttribute("selectedTownId",townId);
        model.addAttribute("keyword",keyword != null ? keyword : "");
        model.addAttribute("departments", departments);
        model.addAttribute("selectedDepartment", department);
        return CINEMAS;
    }

    //--------------importer cinémas----------------------------------------
    /**
     * Imports cinemas from the configured CSV file<p>
     * Import statistics are stored as flash messages
     * @param redirectAttributes the Spring {@link RedirectAttributes}
     * used for flash messages
     * @return a redirect to the cinema administration page
     */
    @GetMapping("/admin/import-cinemas")
    public String importCinemas(RedirectAttributes redirectAttributes){
        try {
            CinemaCsvImporter.ImportResult result = cinemaCsvImporter.importFromCsv();
            redirectAttributes.addFlashAttribute("message",
                    result.imported() + " cinémas importés, " + result.skipped() + " ignorés");
        }catch (Exception e){
            redirectAttributes.addFlashAttribute("error", "Erreur import : " + e.getMessage());
        }
        return ADMIN_REDIRECT;
    }

    //------------pour admin - page de gestion des cinémas-------------
    /**
     * Displays the cinema administration page
     * <p>
     * Supports:
     * <ul>
     *     <li>keyword search</li>
     *     <li>pagination</li>
     *     <li>sorting</li>
     *     <li>cinema edition</li>
     * </ul>
     *
     * @param keyword the search keyword
     * @param page the requested page number
     * @param sortField the selected sorting field
     * @param sortDir the sorting direction
     * @param editId the identifier of the cinema being edited
     * @param model the Spring {@link Model} used to pass data to the view
     * @return the cinema administration page
     */
    @GetMapping("/admin/cinemas")
    public String adminCinemas(@RequestParam(required = false) String keyword,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "name") String sortField,
                               @RequestParam(defaultValue = "asc")String sortDir,
                               @RequestParam(required = false) Long editId,
                               Model model) {

        Page<Cinema> cinemaPage = cinemaService.searchAdmin(keyword, sortField, sortDir, page);

        Cinema cinemaToEdit = (editId != null)
                ? cinemaService.findById(editId).orElse(new Cinema())
                : new Cinema();

        model.addAttribute("cinemaPage",    cinemaPage);
        model.addAttribute(CINEMAS,cinemaPage.getContent());
        model.addAttribute("pages",         new int[cinemaPage.getTotalPages()]);
        model.addAttribute("currentPage",   page);
        model.addAttribute("keyword",       keyword);
        model.addAttribute("sortField",     sortField);
        model.addAttribute("sortDir",       sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("towns",         townService.getAll());
        model.addAttribute("cinemaToEdit",   cinemaToEdit);

        return "admin-cinemas";
    }

    //--------------créer ou modifier un cinéma -----------------------
    /**
     * Creates or updates a cinema <p>
     * The submitted cinema form is validated before saving.
     * If validation fails, the user is redirected back to the cinema administration page
     * with an error message stored in flash attributes
     *
     * @param form the validated cinema form containing cinema data
     * @param bindingResult the validation result for the submitted form
     * @param ra the Spring {@link RedirectAttributes} used to pass flash messages
     * @return a redirect to the cinema administration page
     */
    @PostMapping("/admin/saveCinema")
    public String saveCinema(@Valid @ModelAttribute CinemaForm form,
                             BindingResult bindingResult,
                             RedirectAttributes ra){

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().isEmpty()
                    ? "Données invalides"
                    : bindingResult.getAllErrors().get(0).getDefaultMessage();

            ra.addFlashAttribute("error", errorMessage);

            return ADMIN_REDIRECT;
        }
        cinemaService.save(
                form.getId(),
                form.getName(),
                form.getAddress(),
                form.getTownId(),
                form.getWebsite(),
                form.getLatitude(),
                form.getLongitude(),
                form.getImageUrl(),
                form.getDepartment()
        );
        return ADMIN_REDIRECT;
    }

    /**
     * Deletes a cinema from the database
     * <p>
     * <strong>Security:</strong> This endpoint is restricted to administrators
     * <p>
     * After attempting to delete the cinema by its ID, the user is redirected
     * back to the cinema management dashboard.
     * @param id      the unique identifier of the cinema to delete
     * @return a redirection URL to the admin cinemas page, or the default redirection if unauthorized
     */
    @GetMapping("/admin/deleteCinema")
    public String deleteCinema(@RequestParam Long id){
        cinemaService.delete(id);
        return ADMIN_REDIRECT;
    }
}
