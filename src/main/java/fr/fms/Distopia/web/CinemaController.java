package fr.fms.Distopia.web;

import fr.fms.Distopia.entities.Cinema;
import fr.fms.Distopia.service.CinemaCsvImporter;
import fr.fms.Distopia.service.CinemaService;
import fr.fms.Distopia.service.TownService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller responsible for handling cinema-related web requests,
 * including both public views and administrative management
 */
@Controller
public class CinemaController {

    @Autowired
    private CinemaService cinemaService;

    @Autowired
    private TownService townService;

    @Autowired
    private CinemaCsvImporter cinemaCsvImporter;

    private static final String CINEMAS = "cinemas";

    //---------pour visiteur — cinémas d'une ville-----------------
    /**
     * Handles the visitor request to display a list of cinemas
     * <p>
     * The results can be optionally filtered by a specific town and/or a search keyword.
     * The method populates the model with the list of available towns for the filter dropdown,
     * the filtered list of cinemas, and the current search criteria to retain the form's state in the view
     *
     * @param townId  the unique identifier of the town to filter by (optional)
     * @param keyword the search term to filter cinemas by name or address (optional)
     * @param model   the Spring {@link Model} used to pass data to the view
     * @return the view name "cinemas"
     */
    @GetMapping("/cinemas")
    public String cinemasByTown(@RequestParam(required = false)Long townId,
                                @RequestParam(required = false) String keyword, Model model){
        model.addAttribute("towns", townService.getAll());
        model.addAttribute(CINEMAS, cinemaService.search(keyword, townId));
        model.addAttribute("selectedTownId", townId);
        model.addAttribute("keyword", keyword != null ? keyword : "");
        return CINEMAS;
    }

    //--------------importer cinémas----------------------------------------
    @GetMapping("/admin/import-cinemas")
    public String importCinemas(RedirectAttributes redirectAttributes){
        try {
            CinemaCsvImporter.ImportResult result = cinemaCsvImporter.importFromCsv();
            redirectAttributes.addFlashAttribute("message",
                    result.imported() + " cinémas importés, " + result.skipped() + " ignorés");
        }catch (Exception e){
            redirectAttributes.addFlashAttribute("error", "Erreur import : " + e.getMessage());
        }
        return "redirect:/admin/cinemas";
    }

    //------------pour admin - page de gestion des cinémas-------------
    /**
     * Displays the cinema management page for administrators
     * <p>
     * <strong>Security:</strong> This endpoint requires the user to be logged in with
     * an "ADMIN" role. If unauthorized, the user is redirected away
     * <p>
     * If an {@code editId} is provided, the method fetches the corresponding cinema
     * and adds it to the model to pre-populate the edit form on the page
     *
     * @param model   the Spring {@link Model} used to pass data to the view
     * @return the view name "admin-cinemas", or a redirection URL if unauthorized
     */
    @GetMapping("/admin/cinemas")
    public String adminCinemas(@RequestParam(required = false) String keyword,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "name") String sortField,
                               @RequestParam(defaultValue = "asc")String sortDir,
                               Model model) {

        Page<Cinema> cinemaPage = cinemaService.searchAdmin(keyword, sortField, sortDir, page);

        model.addAttribute("cinemaPage",    cinemaPage);
        model.addAttribute("cinemas",       cinemaPage.getContent());
        model.addAttribute("pages",         new int[cinemaPage.getTotalPages()]);
        model.addAttribute("currentPage",   page);
        model.addAttribute("keyword",       keyword);
        model.addAttribute("sortField",     sortField);
        model.addAttribute("sortDir",       sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("towns",         townService.getAll());

        return "admin-cinemas";
    }

    //--------------créer ou modifier un cinéma -----------------------
    /**
     * Handles the creation or modification of a cinema
     * <p>
     * <strong>Security:</strong> This endpoint is restricted to administrators
     * <p>
     * If an ID is provided, it updates the existing cinema; otherwise, it creates a new one.
     * Upon successful processing, it redirects the user back to the cinema management page
     *
     * @param id      the unique identifier of the cinema to update (null for creation)
     * @param name    the name of the cinema
     * @param address the physical address of the cinema
     * @param townId  the identifier of the town where the cinema is located (optional)
     * @return a redirection URL to the admin cinemas page, or the default redirection if unauthorized
     */
    @PostMapping("/admin/saveCinema")
    public String saveCinema(@RequestParam(required = false) Long id,
                             @RequestParam String name,
                             @RequestParam String address,
                             @RequestParam(required = false) Long townId,
                             @RequestParam(required = false) String website,
                             @RequestParam(required = false) Double latitude,
                             @RequestParam(required = false) Double longitude){
        cinemaService.save(id, name, address, townId, website, latitude, longitude);
        return "redirect:/admin/cinemas";
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
        return "redirect:/admin/cinemas";
    }
}
