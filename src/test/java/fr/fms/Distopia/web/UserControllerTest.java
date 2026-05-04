package fr.fms.Distopia.web;

import fr.fms.Distopia.entities.Role;
import fr.fms.Distopia.entities.User;
import fr.fms.Distopia.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    @Mock
    private UserService userService;
    @Mock
    private Model model;
    @Mock
    private HttpSession session;

    @InjectMocks
    private UserController userController;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("John");
        user.setRole(Role.USER);
    }

    //--------------------------tests for loginPage() ---------------------
    @Test
    @DisplayName("loginPage() - returns 'login' view")
    void loginPage_ShouldReturnLoginView() {
        String view = userController.loginPage(null, session,model);

        assertThat(view).isEqualTo("login");
    }

    @Test
    @DisplayName("loginPage() - stores redirect param in session when present")
    void loginPage_ShouldStoresRedirectParamInSessionWhenPresent() {
        userController.loginPage("/seances?movieId=1&cinemaId=2",session,model);

        verify(session).setAttribute("redirectAfterLogin","/seances?movieId=1&cinemaId=2");
    }

    @Test
    @DisplayName("loginPage() - does not touch session when redirect is null")
    void loginPage_ShouldNotTouchSessionWhenRedirectIsNull() {
        userController.loginPage(null,session,model);

        verify(session, never()).setAttribute(eq("redirectAfterLogin"),any());
    }

    @Test
    @DisplayName("loginPage() - adds redirect param to model")
    void loginPage_ShouldAddRedirectPAramToModel() {
        userController.loginPage("/my-reservations",session,model);

        verify(session).setAttribute("redirectAfterLogin","/my-reservations");
    }
}
