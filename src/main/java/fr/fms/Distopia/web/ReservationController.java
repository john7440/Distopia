package fr.fms.Distopia.web;

import fr.fms.Distopia.entities.User;
import fr.fms.Distopia.exceptions.NoSeatsAvailableException;
import fr.fms.Distopia.service.ReservationService;
import fr.fms.Distopia.utils.SessionUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller responsible for handling user reservations and related web requests
 */
@Controller
public class ReservationController {
    @Autowired
    private ReservationService reservationService;

    //----affichage mes reservations-------------------
    /**
     * Displays the personal reservations page for the currently logged-in user
     * <p>
     * <strong>Security:</strong> This endpoint requires an active user session.
     * If the user is not authenticated, they are automatically redirected to the login page
     *
     * @param model   the Spring {@link Model} used to pass data to the view
     * @param session the current {@link HttpSession} used to retrieve the authenticated user
     * @return the view name "my-reservations", or a redirection URL to the login page if unauthenticated
     */
    @GetMapping("/my-reservations")
    public String myReservations(Model model, HttpSession session){
        if (SessionUtils.isNotConnected(session)) return "redirect:/login";
        User user = SessionUtils.getUser(session);
        model.addAttribute("reservations", reservationService.getByUser(user.getId()));
        return "my-reservations";
    }

    //----------------------faire une réservation--------------------------
    /**
     * Processes a reservation request for a specific seance
     * <p>
     * <strong>Security:</strong> This endpoint requires an active user session
     * <p>
     * <strong>Business Logic:</strong> This method attempts to book the requested number of seats.
     * It intercepts any {@link NoSeatsAvailableException} thrown by the service layer (if there
     * are not enough seats left) and utilizes flash attributes to display a temporary success or
     * error message to the user after the redirection.
     *
     * @param seanceId           the unique identifier of the seance being booked
     * @param quantity           the number of seats to reserve (defaults to 1 if not explicitly provided)
     * @param session            the current {@link HttpSession} used to retrieve the authenticated user
     * @param redirectAttributes the Spring {@link RedirectAttributes} used to pass flash messages across the redirect
     * @return a redirection URL to the user's reservations page, or to the login page if unauthenticated
     */
    @PostMapping("/reserve")
    public String reserveSeance(@RequestParam Long seanceId, @RequestParam(defaultValue = "1") int quantity,
                                @RequestParam(required = false) Boolean confirmed,
                                HttpSession session, RedirectAttributes redirectAttributes){
        if (SessionUtils.isNotConnected(session)) return "redirect:/login";
        User user = SessionUtils.getUser(session);

        boolean alreadyBooked = reservationService.existsByUserAndSeance(user.getId(), seanceId);

        if (alreadyBooked && (confirmed == null || !confirmed)){
            redirectAttributes.addFlashAttribute("confirmNeeded", true);
            redirectAttributes.addFlashAttribute("pendingSeanceId", seanceId);
            redirectAttributes.addFlashAttribute("pendingQuantity", quantity);
            return "redirect:/seances?movieId=" + reservationService.getMovieIdBySeance(seanceId) + "&cinemaId=" +
                    reservationService.getCinemaIdBySeance(seanceId);
        }

        try {
            reservationService.createReservation(seanceId, user.getId(), quantity);
            redirectAttributes.addFlashAttribute("message", "Reservation bien enregistrée");
        } catch (NoSeatsAvailableException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/my-reservations";
    }
}
