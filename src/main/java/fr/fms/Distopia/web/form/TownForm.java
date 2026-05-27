package fr.fms.Distopia.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Form object used to validate town creation and edition input<p>
 * This class contains only the fields submitted by the town administration form
 * and avoids exposing the full Town entity directly to the web layer
 */
@Getter
@Setter
public class TownForm {
    /**
     * The town identifier, null when creating a new town
     */
    private Long id;

    /**
     * The town name
     */
    @NotBlank(message = "Le nom de la ville est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom de la ville doit contenir entre 2 et 100 caractères")
    private String name;
}
