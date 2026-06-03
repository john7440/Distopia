package fr.fms.Distopia.web;

import fr.fms.Distopia.exceptions.NoSeatsAvailableException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    //--------------------tests handleConstraintViolationException()----------------------------

    @Test
    @DisplayName("handleConstraintViolationException() - should add validation message and redirect to index")
    void handleConstraintViolationException_ShouldAddValidationMessageAndRedirectToIndex() {
        RedirectAttributes ra = mock(RedirectAttributes.class);

        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Nom obligatoire");

        ConstraintViolationException exception = new ConstraintViolationException(Set.of(violation));

        String view = handler.handleConstraintViolationException(exception, ra);

        assertThat(view).isEqualTo("redirect:/index");
        verify(ra).addFlashAttribute("error", "Nom obligatoire");
    }

    @Test
    @DisplayName("handleConstraintViolationException() - should use default message when no violation exists")
    void handleConstraintViolationException_ShouldUseDefaultMessageWhenNoViolationExists() {
        RedirectAttributes ra = mock(RedirectAttributes.class);

        ConstraintViolationException exception = new ConstraintViolationException(Set.of());

        String view = handler.handleConstraintViolationException(exception, ra);

        assertThat(view).isEqualTo("redirect:/index");

        verify(ra).addFlashAttribute("error", "Données invalides");
    }

    //--------------------test handleNoSeatsAvailable()---------------------

    @Test
    @DisplayName("handleNoSeatsAvailable() - should add error message and redirect to reservations")
    void handleNoSeatsAvailable_ShouldAddErrorMessageAndRedirectToReservations() {
        RedirectAttributes ra = mock(RedirectAttributes.class);

        NoSeatsAvailableException exception = new NoSeatsAvailableException("Plus de places disponibles");

        String view = handler.handleNoSeatsAvailable(exception, ra);

        assertThat(view).isEqualTo("redirect:/my-reservations");

        verify(ra).addFlashAttribute("error", "Plus de places disponibles");
    }

    //--------------------test for  handleGenericException()-----------------
    @Test
    @DisplayName("handleGenericException() - should add generic error message and redirect to index")
    void handleGenericException_ShouldAddGenericErrorMessageAndRedirectToIndex() {
        RedirectAttributes ra = mock(RedirectAttributes.class);

        Exception exception = new RuntimeException("unexpected error");

        String view = handler.handleGenericException(exception, ra);

        assertThat(view).isEqualTo("redirect:/index");

        verify(ra).addFlashAttribute("error", "Une erreur inattendue est survenue");
    }
}
