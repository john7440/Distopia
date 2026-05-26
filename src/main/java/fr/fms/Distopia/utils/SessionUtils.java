package fr.fms.Distopia.utils;


import fr.fms.Distopia.entities.Role;
import fr.fms.Distopia.entities.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class related to user authentication and session management
 * <p>
 * Provides helper methods to:
 * <ul>
 *     <li>retrieve the currently authenticated user</li>
 *     <li>check authentication status</li>
 *     <li>check administrator privileges</li>
 * </ul>
 */
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
     * Retrieves the currently authenticated user from the
     * Spring Security context
     * @return the connected user, or null if no authenticated user exists
     */
    public static User getConnectedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated())return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof User user) {
            return user;
        }
        return null;
    }

    /**
     * Checks whether no user is currently authenticated
     * @return true if no user is connected, otherwise false
     */
    public static boolean isNotConnected() {
        return getConnectedUser() == null;
    }

    /**
     * Checks whether the connected user is not an administrator
     *
     * @return true if the user is not authenticated
     * or does not have the ADMIN role
     */
    public static boolean isNotAdmin() {
        User user = getConnectedUser();
        return user == null || user.getRole() != Role.ADMIN;
    }
}
