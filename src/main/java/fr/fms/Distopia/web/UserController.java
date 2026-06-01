package fr.fms.Distopia.web;

import fr.fms.Distopia.entities.User;
import fr.fms.Distopia.service.UserService;
import fr.fms.Distopia.web.form.RegisterForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * Controller responsible for authentication modal redirections and user registration
 */
@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    //-------------affichage modale de connexion -------------------
    /**
     * Displays the login modal
     * @return the redirection to the modal
     */
    @GetMapping("/login")
    public String login() {
        return "redirect:/?openLogin";
    }

    //------------------------modale inscription---------------------------------
    /**
     * Displays the registration form (modal)
     * @return the redirection to the modal
     */
    @GetMapping("/register")
    public String registerPage() {
        return "redirect:/?openRegister";
    }

    /**
     * Registers a new user account
     * <p>
     * The submitted registration form is validated before creating the user.
     * If validation fails, or if the username/email is already used,
     * the user is redirected back to the homepage with the registration modal opened
     * and an error message stored in flash attributes
     *
     * @param form the validated registration form containing username, email and password
     * @param bindingResult the validation result for the submitted form
     * @param ra the Spring {@link RedirectAttributes} used to pass flash messages
     * @return a redirect to the homepage with registration status parameters
     */
    @PostMapping("/register")
    public String register(@Valid RegisterForm form, BindingResult bindingResult, RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            ra.addFlashAttribute("error",
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/?openRegister&registerError";
        }

        Optional<User> result = userService.register(
                form.getUsername(),
                form.getEmail(),
                form.getPassword()
        );
        if (result.isEmpty()) {
            ra.addFlashAttribute("error", "Nom d'utilisateur ou email déjà utilisé");

            return "redirect:/?openRegister&registerError";
        }
        return "redirect:/?registered";
    }
}
