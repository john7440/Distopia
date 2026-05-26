package fr.fms.Distopia.web.form;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Form object used to validate cinema creation and edition input<p>
 * This class contains only the fields submitted by the cinema administration form
 * and avoids exposing the full Cinema entity directly to the web layer
 */
@Getter
@Setter
public class CinemaForm {

    /**
     * The cinema identifier, null when creating a new cinema
     */
    private Long id;

    /**
     * The cinema name
     */
    @NotBlank(message = "Le nom du cinéma est obligatoire")
    @Size(min = 2, max = 120, message = "Le nom doit contenir entre 2 et 120 caractères")
    private String name;

    /**
     * The cinema address
     */
    @NotBlank(message = "L'adresse est obligatoire")
    @Size(max = 255, message = "L'adresse ne doit pas dépasser 255 caractères")
    private String address;

    /**
     * The associated town identifier
     */
    private Long townId;

    /**
     * The cinema website URL
     */
    @Size(max = 255, message = "L'URL du site web ne doit pas dépasser 255 caractères")
    private String website;

    /**
     * The cinema latitude
     */
    @DecimalMin(value = "-90.0", message = "La latitude doit être supérieure ou égale à -90")
    @DecimalMax(value = "90.0", message = "La latitude doit être inférieure ou égale à 90")
    private Double latitude;

    /**
     * The cinema longitude
     */
    @DecimalMin(value = "-180.0", message = "La longitude doit être supérieure ou égale à -180")
    @DecimalMax(value = "180.0", message = "La longitude doit être inférieure ou égale à 180")
    private Double longitude;

    /**
     * The cinema image URL or local image path
     */
    @Size(max = 500, message = "L'URL de l'image ne doit pas dépasser 500 caractères")
    private String imageUrl;

    /**
     * The cinema department code
     */
    @Pattern(regexp = "^$|^\\d{2,3}$", message = "Le département doit contenir 2 ou 3 chiffres")
    private String department;
}
