package fr.fms.Distopia.utils;

import fr.fms.Distopia.entities.User;
import jakarta.servlet.http.HttpSession;

public class SessionUtils {
    private SessionUtils() {
        /* This utility class should not be instantiated */
    }

    public static final String REDIRECTION = "redirect:/index";

    /**
     * Checks whether the currently authenticated user doesn't
     * have the "ADMIN" role
     *
     * @param session the current HTTP session
     * @return True if the user is absent or not an admin,
     *         false otherwise
     */
    public static boolean isNotAdmin(HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        return user == null || !user.getRole().equals("ADMIN");
    }

}
