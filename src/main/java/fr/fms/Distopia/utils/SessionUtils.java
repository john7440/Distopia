package fr.fms.Distopia.utils;


import fr.fms.Distopia.entities.Role;
import fr.fms.Distopia.entities.User;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


public class SessionUtils {
    private SessionUtils() {
        /* This utility class should not be instantiated */
    }

    /**
     * The default URL path used for redirection (e.g., when an unauthenticated
     * user attempts to access a protected resource)
     */
    public static final String REDIRECTION = "redirect:/index";

    public static User getConnectedUser() {
        @Nullable Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated())return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof User user) {
            return user;
        }
        return null;
    }
    public static boolean isNotConnected() {
        return getConnectedUser() == null;
    }
    public static boolean isNotAdmin() {
        User user = getConnectedUser();
        return user == null || user.getRole() != Role.ADMIN;
    }


}
