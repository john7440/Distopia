package fr.fms.Distopia.web;

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

        ra.addFlashAttribute("error", message);

        return "redirect:/index";
    }
}
