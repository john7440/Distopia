package fr.fms.Distopia.web;


import fr.fms.Distopia.entities.Reservation;
import fr.fms.Distopia.entities.Role;
import fr.fms.Distopia.entities.User;
import fr.fms.Distopia.service.ReservationService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationControllerTest {
    @Mock
    private ReservationService reservationService;
    @Mock
    private HttpSession session;
    @Mock
    private Model model;
    @Mock
    private RedirectAttributes redirectAttributes;
    @InjectMocks
    private ReservationController reservationController;

    private User connectedUser;

    @BeforeEach
    void setUp(){
        connectedUser = new User();
        connectedUser.setId(1L);
        connectedUser.setUsername("username");
        connectedUser.setRole(Role.USER);

    }

    //------------------------------test for myReservations()-------------------
    @Test
    @DisplayName("myReservations() - return 'my-reservations' view when user is connected")
    void myReservations_ShouldReturnMyReservationsView_WhenUserIsConnected(){
        when(session.getAttribute("connectedUser")).thenReturn(connectedUser);
        when(reservationService.getByUser(1L)).thenReturn(List.of());

        String view = reservationController.myReservations(model,session);

        assertThat(view).isEqualTo("my-reservations");
    }

    @Test
    @DisplayName("myReservations() - redirect to login when user is not connected")
    void myReservations_ShouldReturnMyReservationsView_WhenUserIsNotConnected(){
        when(session.getAttribute("connectedUser")).thenReturn(null);

        String view  = reservationController.myReservations(model,session);

        assertThat(view).isEqualTo("redirect:/login");
    }

    @Test
    @DisplayName("myReservation() - adds user reservations to the model")
    void myReservations_ShouldAddReservationsToTheModel(){
        Reservation resa = new Reservation();
        when(session.getAttribute("connectedUser")).thenReturn(connectedUser);
        when(reservationService.getByUser(1L)).thenReturn(List.of(resa));

        reservationController.myReservations(model,session);

        verify(model).addAttribute("reservations", List.of(resa));
    }
}
