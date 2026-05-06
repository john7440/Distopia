package fr.fms.Distopia.web;

import fr.fms.Distopia.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    public String loginPage(@RequestParam(required = false)String error,Model model) {
        if (error != null) {
            model.addAttribute("error", "Identifiants incorrects");
        }
        return "login";
    }

    //----------------------------inscription-----------------------------------------

    /**
     * Displays the registration form
     * @return the name of the register template
     */
    @GetMapping("/register")
    public String registerPage() {
        return "register";
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
    public String register(@RequestParam String username, @RequestParam String password,Model model) {
        return userService.register(username, password)
                .map(user -> "redirect:/login?registered=true")
                .orElseGet(() -> {model.addAttribute("error", "Ce nom d'utilisateur est déjà pris");
                    return "register";
                });
    }
}
