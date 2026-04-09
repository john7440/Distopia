package fr.fms.Distopia.web;


import fr.fms.Distopia.dao.UserRepository;
import fr.fms.Distopia.entities.Role;
import fr.fms.Distopia.entities.User;
import fr.fms.Distopia.service.UserService;
import fr.fms.Distopia.utils.SessionUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    //-------------affichage page de connexion -------------------

    /**
     * Displays the login page
     * @return the name of the login template
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    //-----------------méthode pour traiter la connexion--------------

    /**
     * Processes the login form submission
     * @param username the username submitted via the login form
     * @param password the plain-text password submitted via the login form
     * @param session  the current HTTP session, used to store the authenticated user
     * @param model the Spring MVC model, used to pass error messages to the view
     * @return  redirect to /index on success and /login on failure
     */
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
        return userService.login(username, password)
                .map(user -> {SessionUtils.setUser(session, user);
                    return SessionUtils.REDIRECTION;
                })
                .orElseGet(() -> {model.addAttribute("error", "Identifiants incorrects");
                    return "login";
                });
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
     * @param session  the current HTTP session, used to store the newly registered user
     * @return a redirect to /register if the username is already taken,
     *         or a redirect to /index on successful registration
     */
    @PostMapping("/register")
    public String register(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
        return userService.register(username, password)
                .map(user -> {SessionUtils.setUser(session, user);
                    return SessionUtils.REDIRECTION;
                })
                .orElseGet(() -> {model.addAttribute("error", "Ce nom d'utilisateur est déjà pris");
                    return "register";
                });
    }

    //-------------------méthode de déconnexion ------------------------------

    /**
     * Logs out the current user by invalidating the HTTP session
     *
     * @param session the current HTTP session to invalidate
     * @return a redirect to /index
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return SessionUtils.REDIRECTION;
    }

    //----------A SUPPRIMER après usage!!!!!!!!!!!!!!!--------------------
    //-----Don't forget to delete it after use!!!!!!!!!!!!!!!!----------------------
    @GetMapping("/createAdmin")
    public String createAdmin() {
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);
        return "redirect:/login";
    }

}
