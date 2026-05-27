package fr.fms.Distopia.web;

import fr.fms.Distopia.exceptions.NoSeatsAvailableException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Global exception handler for web controllers<p>
 * Centralizes common exception handling and redirects users
 * with appropriate flash messages instead of displaying technical errors
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String ERROR = "error";

    /**
     * Handles validation errors on request parameters<p>
     * Useful for controller-level validation
     * such as {@code @Min}, {@code @Max} or {@code @NotBlank}
     *
     * @param e the validation exception
     * @param ra the Spring {@link RedirectAttributes}
     * used to send flash messages
     * @return a redirect to the home page
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public String handleConstraintViolationException(ConstraintViolationException e, RedirectAttributes ra) {
        logger.warn("Validation error: {}",e.getMessage());

        String message = e.getConstraintViolations()
                .stream().findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse("Données invalides");

        ra.addFlashAttribute(ERROR, message);

        return "redirect:/index";
    }

    /**
     * Handles reservation errors when no seats are available
     *
     * @param e the no seats available exception
     * @param ra the Spring {@link RedirectAttributes}
     * used to send flash messages
     * @return a redirect to the user's reservations page
     */
    @ExceptionHandler(NoSeatsAvailableException.class)
    public String handleNoSeatsAvailable(NoSeatsAvailableException e, RedirectAttributes ra){
        logger.warn("Reservation failed: {}",e.getMessage());

        ra.addFlashAttribute(ERROR, e.getMessage());

        return "redirect:/my-reservations";
    }

    /**
     * Handles unexpected errors
     *
     * @param e the unexpected exception
     * @param ra the Spring {@link RedirectAttributes}
     * used to send flash messages
     * @return a redirect to the home page
     */
    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception e, RedirectAttributes ra) {
        logger.error("Unexpected app error",e);
        ra.addFlashAttribute(ERROR, "Une erreur inattendue est survenue");

        return "redirect:/index";
    }
}
