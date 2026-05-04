package fr.fms.Distopia.utils;

import fr.fms.Distopia.entities.Role;
import fr.fms.Distopia.entities.User;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionUtilsTest {
    @Mock
    private HttpSession session;

    private User adminUser;
    private User regularUser;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setRole(Role.ADMIN);

        regularUser = new User();
        regularUser.setId(2L);
        regularUser.setRole(Role.USER);
    }

    //-------------------Tests for isNotConnected()-------------------
    @Test
    @DisplayName("isNotConnected() - returns true when no user in session")
    void isNotConnected_shouldReturnTrueWhenNoUserInSession() {
        when(session.getAttribute("connectedUser")).thenReturn(null);

        assertThat(SessionUtils.isNotConnected(session)).isTrue();
    }

    @Test
    @DisplayName("isNotConnected() - returns false when a user is in session")
    void isNotConnected_shouldReturnFalseWhenUserIsInSession() {
        when(session.getAttribute("connectedUser")).thenReturn(regularUser);

        assertThat(SessionUtils.isNotConnected(session)).isFalse();
    }
}
