package fr.fms.Distopia.web;

import fr.fms.Distopia.entities.User;
import fr.fms.Distopia.service.UserService;
import fr.fms.Distopia.web.form.RegisterForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.validation.BindingResult;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    @Mock
    private UserService userService;
    @Mock
    private BindingResult bindingResult;
    @Mock
    private RedirectAttributes redirectAttributes;
    @InjectMocks
    private UserController userController;

    private RegisterForm form;

    @BeforeEach
    void setUp() {
        form = new RegisterForm();
        form.setUsername("john");
        form.setEmail("john@mail.com");
        form.setPassword("password123");
    }

    //------------------------test for login()-------------------------
    @Test
    @DisplayName("login() - should redirect to login modal")
    void login_ShouldRedirectToLoginModal() {
        String view = userController.login();

        assertThat(view).isEqualTo("redirect:/?openLogin");
    }

    //------------------------test for registerPage()-------------------------

    @Test
    @DisplayName("registerPage() - should redirect to register modal")
    void registerPage_ShouldRedirectToRegisterModal() {
        String view = userController.registerPage();

        assertThat(view).isEqualTo("redirect:/?openRegister");
    }

    //------------------------tests for register() (POST) --------------------------
    @Test
    @DisplayName("register() - redirects with registered flag when registration succeeds")
    void register_ShouldRedirectWithRegisteredFlag_WhenRegistrationSucceeds() {
        User user = new User();
        user.setId(1L);
        user.setUsername("john");

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.register("john", "john@mail.com", "password123"))
                .thenReturn(Optional.of(user));

        String view = userController.register(form, bindingResult, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/?registered");
        verify(userService).register("john", "john@mail.com", "password123");
        verify(redirectAttributes, never()).addFlashAttribute(eq("error"), any());
    }

    @Test
    @DisplayName("register() - redirects with error when form validation fails")
    void register_ShouldRedirectWithError_WhenFormValidationFails() {
        ObjectError error = new ObjectError("registerForm", "Le mot de passe est obligatoire");

        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getAllErrors()).thenReturn(List.of(error));

        String view = userController.register(form, bindingResult, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/?openRegister&registerError");
        verify(redirectAttributes).addFlashAttribute(
                        "error", "Le mot de passe est obligatoire");
        verify(userService, never()).register(any(), any(), any());
    }

    @Test
    @DisplayName("register() - redirects with error when username or email already exists")
    void register_ShouldRedirectWithError_WhenUserAlreadyExists() {
        when(bindingResult.hasErrors()).thenReturn(false);

        when(userService.register("john", "john@mail.com", "password123"))
                .thenReturn(Optional.empty());

        String view = userController.register(form, bindingResult, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/?openRegister&registerError");
        verify(redirectAttributes).addFlashAttribute("error",
                        "Nom d'utilisateur ou email déjà utilisé");
        verify(userService).register("john", "john@mail.com", "password123");
    }
}
