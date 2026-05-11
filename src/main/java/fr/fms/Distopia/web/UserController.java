package fr.fms.Distopia.web;

import fr.fms.Distopia.entities.User;
import fr.fms.Distopia.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class UserController {

    @Autowired
    private UserService userService;


    //-------------affichage page de connexion -------------------
    /**
     * Displays the login page
     * @return the name of the login template
     */
    @GetMapping("/login")
    public String login() {
        return "redirect:/?openLogin";
    }

    //----------------------------inscription-----------------------------------------

    /**
     * Displays the registration form
     * @return the name of the register template
     */
    @GetMapping("/register")
    public String registerPage() {
        return "redirect:/?openRegister";
    }

    /**
     * Processes the registration form submission
     *
     * @param username the desired username submitted via the registration form
     * @param password the plain-text password submitted via the registration form
     * @return a redirect to /register if the username is already taken,
     *         or a redirect to /index on successful registration
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
