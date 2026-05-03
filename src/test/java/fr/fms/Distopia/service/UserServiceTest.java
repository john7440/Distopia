package fr.fms.Distopia.service;

import fr.fms.Distopia.dao.UserRepository;
import fr.fms.Distopia.entities.Role;
import fr.fms.Distopia.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("john");
        user.setPassword("$2a$10$hashedpassword");
        user.setRole(Role.USER);
    }

    // -------tests de la méthode login()---------------------------------------

    @Test
    @DisplayName("login() - returns user when credentials are valid")
    void login_shouldReturnUser_whenCredentialsAreValid() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("rawPassword", user.getPassword())).thenReturn(true);

        Optional<User> result = userService.login("john", "rawPassword");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("john");
    }

    @Test
    @DisplayName("login() - returns empty when username not found")
    void login_shouldReturnEmpty_whenUsernameNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        Optional<User> result = userService.login("unknown", "anyPassword");

        assertThat(result).isEmpty();
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("login() - returns empty when password does not match")
    void login_shouldReturnEmpty_whenPasswordIsWrong() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", user.getPassword())).thenReturn(false);

        Optional<User> result = userService.login("john", "wrongPassword");

        assertThat(result).isEmpty();
    }

    // -----------tests de la méthode register()--------------------------------------------------

    @Test
    @DisplayName("register() - creates and returns new user when username is available")
    void register_shouldCreateUser_whenUsernameIsAvailable() {
        when(userRepository.findByUsername("newUser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("rawPassword")).thenReturn("$2a$10$encodedHash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<User> result = userService.register("newUser", "rawPassword");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("newUser");
        assertThat(result.get().getPassword()).isEqualTo("$2a$10$encodedHash");
        assertThat(result.get().getRole()).isEqualTo(Role.USER);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register() - returns empty when username is already taken")
    void register_shouldReturnEmpty_whenUsernameAlreadyExists() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        Optional<User> result = userService.register("john", "anyPassword");

        assertThat(result).isEmpty();
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("register() - encodes password before saving")
    void register_shouldEncodePassword_beforeSaving() {
        when(userRepository.findByUsername("secureUser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("mySecret")).thenReturn("encodedSecret");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<User> result = userService.register("secureUser", "mySecret");

        assertThat(result.get().getPassword()).isEqualTo("encodedSecret");
        assertThat(result.get().getPassword()).doesNotContain("mySecret");
    }
}
