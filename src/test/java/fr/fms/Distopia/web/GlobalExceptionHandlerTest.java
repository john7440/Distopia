package fr.fms.Distopia.web;

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
}
