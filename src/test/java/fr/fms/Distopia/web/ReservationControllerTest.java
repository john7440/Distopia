package fr.fms.Distopia.web;


import fr.fms.Distopia.entities.Reservation;
import fr.fms.Distopia.entities.Role;
import fr.fms.Distopia.entities.User;
import fr.fms.Distopia.exceptions.NoSeatsAvailableException;
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

    //--------------------------tests for reserveSeance()-----------------
    @Test
    @DisplayName("reserveSeance() - redirects to login when user is not connected")
    void reserveSeance_ShouldRedirectToLogin_WhenUserIsNotConnected(){
        when(session.getAttribute("connectedUser")).thenReturn(null);

        String view = reservationController.reserveSeance(1L, 2,null,session,redirectAttributes);

        assertThat(view).isEqualTo("redirect:/login");
        verify(reservationService,never()).createReservation(any(),any(),anyInt());
    }

    @Test
    @DisplayName("reserveSeance() - creates reservation and redirect to my-reservations on success")
    void reserveSeance_ShouldCreatesReservationAndRedirectToMyReservations_OnSuccess(){
        when(session.getAttribute("connectedUser")).thenReturn(connectedUser);
        when(reservationService.existsByUserAndSeance(1L,1L)).thenReturn(false);

        String view = reservationController.reserveSeance(1L,2,null,session,redirectAttributes);

        assertThat(view).isEqualTo("redirect:/my-reservations");
        verify(reservationService).createReservation(1L,1L,2);
        verify(redirectAttributes).addFlashAttribute(eq("message"), any());

    }

    @Test
    @DisplayName("reserveSeance() - adds error flash attribute when no seats available")
    void reserveSeance_ShouldAddErrorFlashAttribute_WhenNoSeatsAvailable(){
        when(session.getAttribute("connectedUser")).thenReturn(connectedUser);
        when(reservationService.existsByUserAndSeance(1L,1L)).thenReturn(false);
        doThrow(new NoSeatsAvailableException("Plus de places disponibles"))
            .when(reservationService).createReservation(1L,1L,2);

        String view = reservationController.reserveSeance(1L,2,null,session,redirectAttributes);

        assertThat(view).isEqualTo("redirect:/my-reservations");
        verify(redirectAttributes).addFlashAttribute(eq("error"), eq("Plus de places disponibles"));
    }

    //--------------------------tests for reserveSeance() / with duplicate booking-----------------
    @Test
    @DisplayName("reserveSeance() - asks for confirmation when already booked and confirmed is null")
    void reserveSeance_ShouldAddConfirmation_WhenAlreadyBookedAndConfirmedIsNull(){
        when(session.getAttribute("connectedUser")).thenReturn(connectedUser);
        when(reservationService.existsByUserAndSeance(1L,1L)).thenReturn(true);
        when(reservationService.getMovieIdBySeance(1L)).thenReturn(10L);
        when(reservationService.getCinemaIdBySeance(1L)).thenReturn(5L);

        String view = reservationController.reserveSeance(1L,1,null,session,redirectAttributes);

        assertThat(view).startsWith("redirect:/seances");
        verify(redirectAttributes).addFlashAttribute("confirmNeeded", true);
        verify(redirectAttributes).addFlashAttribute("pendingSeanceId", 1L);
        verify(redirectAttributes).addFlashAttribute("pendingQuantity", 1);
        verify(reservationService,never()).createReservation(any(),any(),anyInt());
    }

    @Test
    @DisplayName("reserveSeance() - bypasses confirmation and books when confirmed is true")
    void reserveSeance_ShouldAddConfirmation_WhenConfirmedIsTrue(){
        when(session.getAttribute("connectedUser")).thenReturn(connectedUser);
        when(reservationService.existsByUserAndSeance(1L,1L)).thenReturn(true);

        String view =  reservationController.reserveSeance(1L,1,true,session,redirectAttributes);

        assertThat(view).isEqualTo("redirect:/my-reservations");
        verify(reservationService).createReservation(1L,1L,1);
    }
}
