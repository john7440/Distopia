package fr.fms.Distopia.web.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Form object used to validate user registration input
 * <p>
 * This class contains only the fields submitted by the registration form
 * and avoids exposing the full {@link fr.fms.Distopia.entities.User} entity
 * directly to the web layer
 */
@Getter
@Setter
public class RegisterForm {
    /**
     * The username chosen by the user
     */
    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    @Size(min = 3, max = 50, message = "Le nom d'utilisateur doit contenir entre 3 et 50 caractères")
    private String username;

    /**
     * The user email address
     */
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    @Size(max = 120, message = "L'email ne doit pas dépasser 120 caractères")
    private String email;

    /**
     * The raw password submitted during registration
     */
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, max = 100, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String password;
}
