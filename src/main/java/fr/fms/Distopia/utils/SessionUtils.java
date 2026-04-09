package fr.fms.Distopia.utils;

import fr.fms.Distopia.entities.Role;
import fr.fms.Distopia.entities.User;
import jakarta.servlet.http.HttpSession;

public class SessionUtils {
    private SessionUtils() {
        /* This utility class should not be instantiated */
    }

    /**
     * The default URL path used for redirection (e.g., when an unauthenticated
     * user attempts to access a protected resource)
     */
    public static final String REDIRECTION = "redirect:/index";

    /**
     * The key used to store and retrieve the connected user object in the HTTP session
     */
    public static final String SESSION_USER = "connectedUser";

    //---------------vérification de connexion----------------------
    /**
     * Checks whether there is no currently authenticated user in the session
     *
     * @param session the current HTTP session
     * @return true if no user is connected, false otherwise
     */
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
        User user = (User) session.getAttribute(SESSION_USER);
        return user == null || user.getRole() != Role.ADMIN;
    }

    //-------------------stocke l'utilisateur connecté en session------------
    /**
     * Stores the authenticated user in the current HTTP session
     *
     * @param session the current HTTP session
     * @param user    the {@link User} object to store in the session
     */
    public static void setUser(HttpSession session, User user) {
        session.setAttribute(SESSION_USER, user);
    }

    //-------------------récupérer l'utilisateur----------------------
    /**
     * Retrieves the currently authenticated user from the session
     *
     * @param session the current HTTP session
     * @return the connected {@link User}, or null if no user is currently stored in the session
     */
    public static User getUser(HttpSession session) {
        return (User) session.getAttribute(SESSION_USER);
    }
}
