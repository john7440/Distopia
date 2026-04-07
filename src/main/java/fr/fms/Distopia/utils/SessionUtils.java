package fr.fms.Distopia.utils;

import fr.fms.Distopia.entities.User;
import jakarta.servlet.http.HttpSession;

public class SessionUtils {
    private SessionUtils() {
        /* This utility class should not be instantiated */
    }

    public static final String REDIRECTION = "redirect:/index";
    public static final String SESSION_USER = "connectedUser";

    //---------------vérification de connexion----------------------
    public static boolean isNotConnected(HttpSession session) {
        return session.getAttribute(SESSION_USER) == null;
    }

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

    //-------------------stocke l'utilisateur connecté en session------------
    public static void setUser(HttpSession session, User user) {
        session.setAttribute(SESSION_USER, user);
    }

    //-------------------récupérer l'utilisateur----------------------
    public static User getUser(HttpSession session) {
        return (User) session.getAttribute(SESSION_USER);
    }
}
