package fr.fms.Distopia.web;

import fr.fms.Distopia.entities.Role;
import fr.fms.Distopia.entities.User;
import fr.fms.Distopia.service.UserService;
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


    //------------------------tests for register() (POST) --------------------------
    @Test
    @DisplayName("register() - stores user in session and redirects to /login registered on success")
    void registerPage_ShouldStoresUserInSessionAndRedirectToIndexOnSuccess() {
        when(userService.register("John","monemail@test.com","pass123")).thenReturn(Optional.of(user));

        String view = userController.register("John","monemail@test.com","pass123");

        assertThat(view).isEqualTo("redirect:/?registered");
    }

    @Test
    @DisplayName("register() - returns 'register' view with error when username is taken")
    void registerPage_ShouldReturnRegisterViewWithErrorWhenUsernameIsTaken() {
        when(userService.register("John","monemail@test.com","pass123")).thenReturn(Optional.empty());

        String view = userController.register("John","monemail@test.com","pass123");

        assertThat(view).isEqualTo("redirect:/?openRegister&registerError");
    }

}
