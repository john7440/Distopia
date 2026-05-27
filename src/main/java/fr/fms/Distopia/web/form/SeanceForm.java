package fr.fms.Distopia.web.form;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Form object used to validate seance creation and edition input
 * <p>
 * This class contains only the fields submitted by the seance administration form
 * and avoids exposing the full Seance entity directly to the web layer
 */
@Getter
@Setter
public class SeanceForm {

    /**
     * The seance identifier, null when creating a new seance
     */
    private Long id;

    /**
     * The scheduled date and time of the seance
     */
    @NotNull(message = "La date de séance est obligatoire")
    private LocalDateTime dateTime;

    /**
     * The number of available seats for the seance
     */
    @Min(value = 1, message = "Le nombre de places doit être au moins de 1")
    @Max(value = 500, message = "Le nombre de places ne peut pas dépasser 500")
    private int availableSeats;

    /**
     * The ticket price for the seance
     */
    @DecimalMin(value = "0.01", message = "Le prix doit être supérieur à 0")
    private double price;

    /**
     * The movie identifier associated with the seance
     */
    @NotNull(message = "Le film est obligatoire")
    private Long movieId;

    /**
     * The cinema identifier associated with the seance
     */
    @NotNull(message = "Le cinéma est obligatoire")
    private Long cinemaId;
}
