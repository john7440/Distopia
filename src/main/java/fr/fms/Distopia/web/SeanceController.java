package fr.fms.Distopia.web;

import fr.fms.Distopia.entities.Seance;
import fr.fms.Distopia.service.CinemaService;
import fr.fms.Distopia.service.MovieService;
import fr.fms.Distopia.service.SeanceService;
import fr.fms.Distopia.web.form.SeanceForm;
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
 * Controller responsible for handling seance related web requests
 * <p>
 * This class manages both public views, such as displaying the schedule for a specific movie,
 * and secure administrative views for managing the life cycle of seances
 */
@Controller
public class SeanceController {

    private final SeanceService seanceService;
    private final MovieService movieService;
    private final CinemaService cinemaService;

    public SeanceController(SeanceService seanceService, MovieService movieService, CinemaService cinemaService) {
        this.seanceService = seanceService;
        this.movieService = movieService;
        this.cinemaService = cinemaService;
    }

    private static final String SEANCES =  "seances";
    private static final String ERROR =  "error";
    private static final String MESSAGE =  "message";
    private static final String REDIRECT_ADMIN_SEANCES = "redirect:/admin/seances";

    //----------------seances d'un film----------------
    /**
     * Handles the visitor request to display all scheduled seances for a specific movie
     * <p>
     * It also passes the current {@code cinemaId} to the view to maintain user context
     * (to allow the user to easily navigate back to the cinema's movie list)
     *
     * @param movieId  the unique identifier of the movie whose schedule is being requested
     * @param model    the Spring {@link Model} used to pass data to the view
     * @param cinemaId the identifier of the current cinema context
     * @return the view name "seances"
     */
    @GetMapping("/seances")
    public String seancesByMovie(@RequestParam Long movieId, Model model,  @RequestParam Long cinemaId){
        model.addAttribute(SEANCES, seanceService.getByMovieAndCinema(movieId, cinemaId));
        model.addAttribute("movieId", movieId);
        model.addAttribute("cinemaId", cinemaId);
        return SEANCES;
    }

    //-------------page de gestion des séances------------
    /**
     * Displays the seance administration page<p>
     * Seances can be filtered by movie keyword and cinema, then sorted by date
     * in ascending or descending order
     *
     * @param keyword the optional movie title keyword
     * @param cinemaId the optional cinema identifier filter
     * @param page the requested page index
     * @param sortField the field used for sorting
     * @param sortDir the sorting direction, either "asc" or "desc"
     * @param model the Spring {@link Model} used to pass data to the view
     * @return the view name "admin-seances"
     */
    @GetMapping("/admin/seances")
    public String adminSeances(@RequestParam(required = false) String keyword,
                               @RequestParam(required = false) Long cinemaId,
                               @RequestParam(defaultValue = "false") boolean showArchived,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "dateTime") String sortField,
                               @RequestParam(defaultValue = "asc") String sortDir,
                               Model model) {

        Page<Seance> seancePage =
                seanceService.searchAdmin(keyword, cinemaId,showArchived, sortField, sortDir, page);

        model.addAttribute("seancePage", seancePage);
        model.addAttribute(SEANCES, seancePage.getContent());
        model.addAttribute("pages", new int[seancePage.getTotalPages()]);
        model.addAttribute("currentPage", page);
        model.addAttribute("keyword", keyword);
        model.addAttribute("cinemaId", cinemaId);
        model.addAttribute("showArchived", showArchived);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("movies", movieService.getAll());
        model.addAttribute("cinemas", cinemaService.getAll());

        return "admin-seances";
    }

    //--------------créer ou modifier une séance----------
    /**
     * Creates or updates a seance<p>
     * The submitted seance form is validated before saving.
     * If validation fails, the user is redirected back to the seance administration page
     * with an error message stored in flash attributes
     *
     * @param form the validated seance form containing seance data
     * @param bindingResult the validation result for the submitted form
     * @param ra the Spring {@link RedirectAttributes} used to pass flash messages
     * @return a redirect to the seance administration page
     */
    @PostMapping("/admin/saveSeance")
    public String saveSeance(@Valid @ModelAttribute SeanceForm form, BindingResult bindingResult,
                             RedirectAttributes ra){
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().isEmpty()
                    ? "Données invalides"
                    : bindingResult.getAllErrors().get(0).getDefaultMessage();

            ra.addFlashAttribute(ERROR,errorMessage);

            return REDIRECT_ADMIN_SEANCES;
        }
        seanceService.save(form.getId(), form.getDateTime(), form.getAvailableSeats(), form.getPrice(),
                form.getMovieId(), form.getCinemaId());

        return REDIRECT_ADMIN_SEANCES;
    }

    //-------------------------supprimer une séance----------------
    /**
     * Deletes a seance from the administration page
     * <p>
     * The deletion is refused if the seance already has reservations.
     *
     * @param id the seance identifier
     * @param redirectAttributes the Spring redirect attributes used for feedback messages
     * @return a redirect to the seance administration page
     */
    @PostMapping("/admin/deleteSeance")
    public String deleteSeance(@RequestParam Long id,
                               RedirectAttributes redirectAttributes) {
        try {
            seanceService.delete(id);
            redirectAttributes.addFlashAttribute(MESSAGE, "Séance supprimée avec succès");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute(ERROR, e.getMessage());
        }

        return REDIRECT_ADMIN_SEANCES;
    }

    /**
     * Deletes all selected seances from the administration page<p>
     * The deletion is refused if at least one selected seance already has reservations
     *
     * @param selectedIds the selected seance identifiers
     * @param keyword the current search keyword
     * @param cinemaId the current cinema filter
     * @param sortField the current sort field
     * @param sortDir the current sort direction
     * @param redirectAttributes the Spring redirect attributes used for feedback messages
     * @return a redirect to the seance administration page
     */
    @PostMapping("/admin/deleteSelectedSeances")
    public String deleteSelectedSeances(@RequestParam(required = false) List<Long> selectedIds,
                                        @RequestParam(required = false) String keyword,
                                        @RequestParam(required = false) Long cinemaId,
                                        @RequestParam(defaultValue = "false") boolean showArchived,
                                        @RequestParam(required = false) String sortField,
                                        @RequestParam(required = false) String sortDir,
                                        RedirectAttributes redirectAttributes) {
        try {
            int deleted = seanceService.deleteSelected(selectedIds);

            if (deleted == 0) {
                redirectAttributes.addFlashAttribute("warning", "Aucune séance sélectionnée!");
            } else {
                redirectAttributes.addFlashAttribute(MESSAGE, deleted + " séance(s) supprimée(s).");
            }
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute(ERROR, e.getMessage());
        }

        return buildAdminSeancesRedirect(keyword, cinemaId,showArchived, sortField, sortDir);
    }

    /**
     * Deletes all seances matching the current admin filters<p>
     * The deletion is refused if at least one matching seance already has reservations.
     *
     * @param keyword the current search keyword
     * @param cinemaId the current cinema filter
     * @param sortField the current sort field
     * @param sortDir the current sort direction
     * @param redirectAttributes the Spring redirect attributes used for feedback messages
     * @return a redirect to the seance administration page
     */
    @PostMapping("/admin/deleteFilteredSeances")
    public String deleteFilteredSeances(@RequestParam(required = false) String keyword,
                                        @RequestParam(required = false) Long cinemaId,
                                        @RequestParam(defaultValue = "false") boolean showArchived,
                                        @RequestParam(required = false) String sortField,
                                        @RequestParam(required = false) String sortDir,
                                        RedirectAttributes redirectAttributes) {
        try {
            int deleted = seanceService.deleteByAdminFilters(keyword, cinemaId, showArchived);

            if (deleted == 0) {
                redirectAttributes.addFlashAttribute("warning", "Aucune séance ne correspond aux filtres actuels");
            } else {
                redirectAttributes.addFlashAttribute(MESSAGE, deleted + " séance(s) supprimée(s) selon les filtres actuels");
            }
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute(ERROR, e.getMessage());
        }

        return buildAdminSeancesRedirect(keyword, cinemaId, showArchived, sortField, sortDir);
    }

    /**
     * Builds the redirect URL to the seance administration page while preserving filters
     *
     * @param keyword the current search keyword
     * @param cinemaId the current cinema filter
     * @param sortField the current sort field
     * @param sortDir the current sort direction
     * @return the redirect URL with query parameters
     */
    private String buildAdminSeancesRedirect(String keyword,
                                             Long cinemaId,
                                             boolean showArchived,
                                             String sortField,
                                             String sortDir) {
        StringBuilder redirect = new StringBuilder(REDIRECT_ADMIN_SEANCES);

        boolean hasParam = false;

        if (keyword != null && !keyword.isBlank()) {
            redirect.append("?keyword=").append(keyword);
            hasParam = true;
        }

        if (cinemaId != null) {
            redirect.append(hasParam ? "&" : "?").append("cinemaId=").append(cinemaId);
            hasParam = true;
        }

        if (showArchived) {
            redirect.append(hasParam ? "&" : "?").append("showArchived=true");
            hasParam = true;
        }

        if (sortField != null && !sortField.isBlank()) {
            redirect.append(hasParam ? "&" : "?").append("sortField=").append(sortField);
            hasParam = true;
        }

        if (sortDir != null && !sortDir.isBlank()) {
            redirect.append(hasParam ? "&" : "?").append("sortDir=").append(sortDir);
        }

        return redirect.toString();
    }
}
