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

@Controller
public class ReservationController {
    @Autowired
    private ReservationService reservationService;

    //----affichage mes reservations-------------------
    @GetMapping("/my-reservations")
    public String myReservations(Model model, HttpSession session){
        if (SessionUtils.isNotConnected(session)) return "redirect:/login";
        User user = SessionUtils.getUser(session);
        model.addAttribute("reservations", reservationService.getByUser(user.getId()));
        return "my-reservations";
    }

    //----------------------faire une réservation--------------------------
    @PostMapping("/reserve")
    public String reserveSeance(@RequestParam Long seanceId, @RequestParam(defaultValue = "1") int quantity,
                                HttpSession session, RedirectAttributes redirectAttributes){
        if (SessionUtils.isNotConnected(session)) return "redirect:/login";
        User user = SessionUtils.getUser(session);
        try {
            reservationService.createReservation(seanceId, user.getId(), quantity);
            redirectAttributes.addFlashAttribute("message", "Reservation bien enregistrée");
        } catch (NoSeatsAvailableException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/my-reservations";
    }

}
