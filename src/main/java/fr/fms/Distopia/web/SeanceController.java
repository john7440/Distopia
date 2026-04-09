package fr.fms.Distopia.web;

import fr.fms.Distopia.service.CinemaService;
import fr.fms.Distopia.service.MovieService;
import fr.fms.Distopia.service.SeanceService;
import fr.fms.Distopia.utils.SessionUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

/**
 * Controller responsible for handling seance related web requests
 * <p>
 * This class manages both public views, such as displaying the schedule for a specific movie,
 * and secure administrative views for managing the life cycle of seances
 */
@Controller
public class SeanceController {
    @Autowired
    private SeanceService seanceService;
    @Autowired
    private MovieService movieService;
    @Autowired
    private CinemaService cinemaService;

    private static final String SEANCES =  "seances";

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
     * Displays the seance management dashboard for administrators
     * <p>
     * <strong>Security:</strong> This endpoint requires the user to be logged in with
     * an "ADMIN" role. Unauthorized users are redirected away
     * <p>
     * The method loads all seances and movies to populate the management tables and dropdowns.
     * If an {@code editId} is provided, it fetches that specific seance and adds it to the
     * model to pre-populate the edit form
     *
     * @param editId  the unique identifier of the seance to edit (optional)
     * @param model   the Spring {@link Model} used to pass data to the view
     * @param session the current {@link HttpSession} used to verify the user's role
     * @return the view name "admin-seances", or a redirection URL if unauthorized
     */
    @GetMapping("/admin/seances")
    public String adminSeances(@RequestParam(required = false)Long editId, Model model, HttpSession session){
        if (SessionUtils.isNotAdmin(session)) return SessionUtils.REDIRECTION;
        model.addAttribute(SEANCES, seanceService.getAll());
        model.addAttribute("movies", movieService.getAll());
        model.addAttribute("cinemas", cinemaService.getAll());
        if (editId != null) {
            seanceService.findById(editId).ifPresent(s -> model.addAttribute("editSeance", s));
        }
        return "admin-seances";
    }

    //--------------créer ou modifier une séance----------
    /**
     * Handles the creation or modification of a seance
     * <p>
     * <strong>Security:</strong> This endpoint is restricted to administrators
     * <p>
     * <strong>Data Conversion:</strong> The scheduling date and time are received as a standard
     * {@link String} from the HTML form and are parsed into a {@link LocalDateTime} object
     * before being sent to the service layer
     *
     * @param id             the unique identifier of the seance to update (null for creation)
     * @param dateTime       the scheduled date and time as a string (must be in ISO-8601 format,"YYYY-MM-DDTHH:mm")
     * @param availableSeats the initial total number of available seats for this screening
     * @param price          the ticket price for this screening
     * @param movieId        the identifier of the movie to be screened
     * @param cinemaId       the identifier of the cinema
     * @param session        the current {@link HttpSession} used to verify the user's role
     * @return a redirection URL to the admin seances page, or the default redirection if unauthorized
     * @throws java.time.format.DateTimeParseException if the {@code dateTime} string cannot be parsed
     */
    @PostMapping("/admin/saveSeance")
    public String saveSeance(@RequestParam(required = false) Long id, @RequestParam String dateTime,@RequestParam int availableSeats,
                             @RequestParam double price, @RequestParam Long movieId,@RequestParam Long cinemaId, HttpSession session){
        if (SessionUtils.isNotAdmin(session)) return SessionUtils.REDIRECTION;
        seanceService.save(id, LocalDateTime.parse(dateTime), availableSeats, price, movieId,cinemaId);
        return "redirect:/admin/seances";
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
     * @param session the current {@link HttpSession} used to verify the user's role
     * @return a redirection URL to the admin seances page, or the default redirection if unauthorized
     */
    @GetMapping("/admin/deleteSeance")
    public String deleteSeance(@RequestParam Long id, HttpSession session){
        if (SessionUtils.isNotAdmin(session)) return SessionUtils.REDIRECTION;
        seanceService.delete(id);
        return "redirect:/admin/seances";
    }
}
