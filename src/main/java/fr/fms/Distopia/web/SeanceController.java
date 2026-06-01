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
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
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
     * @param cinemaId the identifier of the current cinema context (optional)
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
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "dateTime") String sortField,
                               @RequestParam(defaultValue = "asc") String sortDir,
                               Model model) {

        Page<Seance> seancePage =
                seanceService.searchAdmin(keyword, cinemaId, sortField, sortDir, page);

        model.addAttribute("seancePage", seancePage);
        model.addAttribute(SEANCES, seancePage.getContent());
        model.addAttribute("pages", new int[seancePage.getTotalPages()]);
        model.addAttribute("currentPage", page);
        model.addAttribute("keyword", keyword);
        model.addAttribute("cinemaId", cinemaId);
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

            ra.addFlashAttribute("error",errorMessage);

            return REDIRECT_ADMIN_SEANCES;
        }
        seanceService.save(form.getId(), form.getDateTime(), form.getAvailableSeats(), form.getPrice(),
                form.getMovieId(), form.getCinemaId());

        return REDIRECT_ADMIN_SEANCES;
    }

    //-------------------------supprimer une séance----------------
    /**
     * Deletes a seance from the database
     * <p>
     * <strong>Security:</strong> This endpoint is restricted to administrators
     * <p>
     * Note: The underlying service method performs a business check and will throw
     * an exception if the seance already has active reservations
     *
     * @param id      the unique identifier of the seance to delete
     * @return a redirection URL to the admin seances page, or the default redirection if unauthorized
     */
    @PostMapping("/admin/deleteSeance")
    public String deleteSeance(@RequestParam Long id){
        seanceService.delete(id);
        return REDIRECT_ADMIN_SEANCES;
    }

    /**
     * Deletes a selection of seances by their IDs
     *
     * @param selectedIds list of seance IDs to delete (maybe {@code null} or empty)
     * @param redirectAttributes flash attributes used to pass feedback to the redirected view
     * @return redirect to the admin seances page
     */
    @PostMapping("/admin/deleteSelectedSeances")
    public String deleteSelectedSeances(@RequestParam(required = false) List<Long> selectedIds,
                                        RedirectAttributes redirectAttributes) {

        int deleted = seanceService.deleteSelected(selectedIds);
        if (deleted == 0) {
            redirectAttributes.addFlashAttribute("warning", "Aucune séance sélectionnée!");
        } else {
            redirectAttributes.addFlashAttribute("message", deleted + " séance(s) supprimée(s)");
        }
        return REDIRECT_ADMIN_SEANCES;
    }

    /**
     * Deletes all seances matching the given admin filters
     * <p>Filters are optional and cumulative:
     * <ul>
     *   <li>{@code keyword} - case-insensitive partial match on movie title or cinema name</li>
     *   <li>{@code cinemaId} - exact match on the cinema ID</li>
     *   <li>{@code movieId} - exact match on the movie ID</li>
     * </ul>
     *
     * <p>After deletion, redirects back to the filtered seances list,
     * preserving the current filter parameters in the URL
     *
     * @param keyword optional search term to filter seances
     * @param cinemaId  optional cinema ID to filter seances
     * @param movieId  optional movie ID to filter seances
     * @param redirectAttributes flash attributes used to pass feedback to the redirected view
     * @return redirect to the admin seances page with current filters applied
     */
    @PostMapping("/admin/deleteFilteredSeances")
    public String deleteFilteredSeances(@RequestParam(required = false) String keyword, @RequestParam(required = false) Long cinemaId,
                                        @RequestParam(required = false) Long movieId, RedirectAttributes redirectAttributes) {

        int deleted = seanceService.deleteByAdminFilters(keyword, cinemaId, movieId);

        if (deleted == 0) {
            redirectAttributes.addFlashAttribute("warning", "Aucune séance ne correspond aux filtres actuels !");
        } else {
            redirectAttributes.addFlashAttribute("message", deleted + " séance(s) supprimée(s) selon les filtres actuels");
        }

        return "redirect:/admin/seances?keyword=" + UriUtils.encode(keyword == null ? "" : keyword, StandardCharsets.UTF_8)
                + (cinemaId != null ? "&cinemaId=" + cinemaId : "")
                + (movieId != null ? "&movieId=" + movieId : "");
    }
}
