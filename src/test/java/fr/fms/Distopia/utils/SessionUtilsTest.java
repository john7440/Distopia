package fr.fms.Distopia.utils;

import fr.fms.Distopia.entities.Role;
import fr.fms.Distopia.entities.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SessionUtilsTest {
    private User adminUser;
    private User regularUser;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUsername("admin");
        adminUser.setRole(Role.ADMIN);

        regularUser = new User();
        regularUser.setId(2L);
        adminUser.setUsername("user");
        regularUser.setRole(Role.USER);
    }

    @AfterEach
    void clearContext() {
        // Nettoie le SecurityContext après chaque test pour éviter les effets de bord
        SecurityContextHolder.clearContext();
    }

    private void authenticate(User user) {
        var auth = new UsernamePasswordAuthenticationToken(
                user,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    //-------------------Tests for isNotConnected()-------------------
    @Test
    @DisplayName("isNotConnected() - returns true when SecurityContext is empty")
    void isNotConnected_shouldReturnTrueWhenNoUserInSession() {
        assertThat(SessionUtils.isNotConnected()).isTrue();
    }

    @Test
    @DisplayName("isNotConnected() - returns false when a user is in session")
    void isNotConnected_shouldReturnFalseWhenUserIsInSession() {
        authenticate(regularUser);
        assertThat(SessionUtils.isNotConnected()).isFalse();
    }

    //----------------tests for isNotAdmin() --------------------------
    @Test
    @DisplayName("isNotAdmin() - returns false when the user in session is admin")
    void isNotAdmin_shouldReturnFalseWhenUserInSessionIsAdmin() {
        authenticate(adminUser);

        assertThat(SessionUtils.isNotAdmin()).isFalse();
    }

    @Test
    @DisplayName("isNotAdmin() - returns true when the user in session is not admin")
    void isNotAdmin_shouldReturnTrueWhenUserIsNotAdmin() {
        authenticate(regularUser);

        assertThat(SessionUtils.isNotAdmin()).isTrue();
    }

    @Test
    @DisplayName("isNotAdmin() - returns true when there is no user in the session")
    void isNotAdmin_shouldReturnTrueWhenThereIsNoUserInSession() {
        assertThat(SessionUtils.isNotAdmin()).isTrue();
    }

    // ------------tests for getConnectedUser()---------------------------
    @Test
    @DisplayName("getConnectedUser() - returns the authenticated user")
    void getConnectedUser_shouldReturnTheAuthenticatedUser() {
        authenticate(adminUser);

        User result = SessionUtils.getConnectedUser();

        assertThat(result).isEqualTo(adminUser);
    }

    @Test
    @DisplayName("getConnectedUser() - returns null when no authentication")
    void getConnectedUser_shouldReturnNull_whenNotAuthenticated() {
        assertThat(SessionUtils.getConnectedUser()).isNull();
    }

    // --------------test REDIRECTION constant ---------------
    @Test
    @DisplayName("REDIRECTION - constant equals 'redirect:/index'")
    void redirection_constantShouldEqualRedirectIndex() {
        assertThat(SessionUtils.REDIRECTION).isEqualTo("redirect:/index");
    }

}
