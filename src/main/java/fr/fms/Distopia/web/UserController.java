package fr.fms.Distopia.web;

import fr.fms.Distopia.entities.User;
import fr.fms.Distopia.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

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
     * Registers a new user account<p>
     * If registration fails (for example because the username
     * or email already exists), the user is redirected
     * to the registration form with an error flag
     *
     * @param username the username chosen by the user
     * @param email the user email address
     * @param password the raw user password
     * @return a redirect to the homepage with registration status flags
     */
    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String email,
                           @RequestParam String password) {
        Optional<User> result = userService.register(username, email, password);
        if (result.isEmpty()){
            return "redirect:/?openRegister&registerError";
        }
        userService.register(username, email, password);
        return "redirect:/?registered";
    }
}
