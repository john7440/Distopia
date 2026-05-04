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

import java.util.Optional;

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

    //------------------tests for login() (POST)------------------------------
    @Test
    @DisplayName("login() - redirects to /index on successful login with no pending redirect")
    void loginPage_ShouldRedirectToIndexOnSuccessfulLoginAndNoRedirectStored() {
        when(userService.login("John","pass123")).thenReturn(Optional.of(user));
        when(session.getAttribute("redirectAfterLogin")).thenReturn(null);

        String view = userController.login("John","pass123", session,model);

        assertThat(view).isEqualTo("redirect:/index");
        verify(session).setAttribute("connectedUser",user);
        verify(session).removeAttribute("redirectAfterLogin");
    }

    @Test
    @DisplayName("login() - redirects to stored redirect Url on successful login")
    void loginPage_ShouldRedirectToStoredRedirectUrlOnSuccessfulLogin() {
        when(userService.login("John","pass123")).thenReturn(Optional.of(user));
        when(session.getAttribute("redirectAfterLogin")).thenReturn("/my-reservations");

        String view = userController.login("John","pass123", session,model);

        assertThat(view).isEqualTo("redirect:/my-reservations");
    }

    @Test
    @DisplayName("login() - returns 'login' view with error on wrong credentials")
    void loginPage_ShouldReturnLoginViewWithErrorOnWrongCredentials() {
        when(userService.login("John","pass123")).thenReturn(Optional.empty());

        String view = userController.login("John","pass123",session, model);

        assertThat(view).isEqualTo("login");
        verify(model).addAttribute("error","Identifiants incorrects");
        verify(session,never()).setAttribute(eq("connectedUser"),any());
    }

    //-----------------test for registerPage()-----------------
    @Test
    @DisplayName("registerPage() - return 'register' view")
    void registerPage_ShouldReturnRegisterView() {
        assertThat(userController.registerPage()).isEqualTo("register");
    }

    //------------------------tests for register() (POST) --------------------------
    @Test
    @DisplayName("register() - stores user in session and redirects to /index on success")
    void registerPage_ShouldStoresUserInSessionAndRedirectToIndexOnSuccess() {
        when(userService.register("John","pass123")).thenReturn(Optional.of(user));

        String view = userController.register("John","pass123", session,model);

        assertThat(view).isEqualTo("redirect:/index");
        verify(session).setAttribute("connectedUser",user);
    }

    @Test
    @DisplayName("register() - returns 'register' view with error when username is taken")
    void registerPage_ShouldReturnRegisterViewWithErrorWhenUsernameIsTaken() {
        when(userService.register("John","pass123")).thenReturn(Optional.empty());

        String view = userController.register("John","pass123", session,model);

        assertThat(view).isEqualTo("register");
        verify(model).addAttribute("error","Ce nom d'utilisateur est déjà pris");
        verify(session, never()).setAttribute(eq("connectedUser"),any());
    }

    //-------------------------------------test for logout()------------------

    @Test
    @DisplayName("logout() - invalidates session and redirect to /index")
    void logout_ShouldInvalidatesSessionAndRedirectToIndexOnSuccess() {
        String view = userController.logout(session);

        assertThat(view).isEqualTo("redirect:/index");
        verify(session).invalidate();
    }
}
