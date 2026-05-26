package fr.fms.Distopia.web.form;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * Form object used to validate movie creation and edition input<p>
 * This class contains only the fields submitted by the movie administration form
 * and avoids exposing the full Movie entity directly to the web layer
 */
@Getter
@Setter
public class MovieForm {

    /**
     * The movie identifier, which is null when creating a new movie
     */
    private Long id;

    /**
     * The TMDB movie identifier
     */
    @Positive(message = "L'identifiant TMDB doit être positif")
    private Long tmdbId;

    /**
     * The movie title
     */
    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 150, message = "Le titre ne doit pas dépasser 150 caractères")
    private String title;

    /**
     * The movie description or synopsis
     */
    @Size(max = 5000, message = "La description ne doit pas dépasser 5000 caractères")
    private String description;

    /**
     * The movie duration in minutes
     */
    @Min(value = 0, message = "La durée ne peut pas être négative")
    @Max(value = 600, message = "La durée ne peut pas dépasser 600 minutes")
    private int duration;

    /**
     * The movie genre
     */
    @Size(max = 80, message = "Le genre ne doit pas dépasser 80 caractères")
    private String genre;

    /**
     * The movie poster image URL
     */
    @Size(max = 500, message = "L'URL de l'image ne doit pas dépasser 500 caractères")
    private String imageUrl;

    /**
     * The movie trailer URL
     */
    @Size(max = 500, message = "L'URL du trailer ne doit pas dépasser 500 caractères")
    private String trailerUrl;

    /**
     * The associated cinema identifiers
     */
    private List<Long> cinemaIds;

    /**
     * The movie release date
     */
    private LocalDate releaseDate;
}
