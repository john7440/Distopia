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
import org.springframework.dao.DataIntegrityViolationException;


import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    // -----------tests de la méthode register()--------------------------------------------------

    @Test
    @DisplayName("register() - creates and returns new user when username is available")
    void register_shouldCreateUser_whenUsernameIsAvailable() {
        when(userRepository.findByUsername("newUser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("rawPassword")).thenReturn("$2a$10$encodedHash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<User> result = userService.register("newUser", "myemail@test.com","rawPassword");

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

        Optional<User> result = userService.register("john", "myemail@test.com","anyPassword");

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

        Optional<User> result = userService.register("secureUser", "myemail@test.com","mySecret");

        assertThat(result.get().getPassword()).isEqualTo("encodedSecret");
        assertThat(result.get().getPassword()).doesNotContain("mySecret");
    }

    @Test
    @DisplayName("register() - handles concurrent username conflict")
    void register_ShouldHandleConcurrentUsernameConflict() {
        User existingUser = new User();
        existingUser.setId(2L);
        existingUser.setUsername("newUser");

        when(userRepository.findByUsername("newUser")).thenReturn(Optional.empty(), Optional.of(existingUser));

        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("Duplicate username"));

        Optional<User> result = userService.register("newUser", "new@test.com", "rawPassword");

        assertThat(result).isEmpty();

        verify(userRepository, times(2)).findByUsername("newUser");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("register() - handles concurrent email conflict")
    void register_ShouldHandleConcurrentEmailConflict() {
        User existingUser = new User();
        existingUser.setId(2L);
        existingUser.setEmail("new@test.com");

        when(userRepository.findByUsername("newUser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty(), Optional.of(existingUser));
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");

        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("Duplicate email"));

        Optional<User> result = userService.register("newUser", "new@test.com", "rawPassword");

        assertThat(result).isEmpty();

        verify(userRepository, times(2)).findByEmail("new@test.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("register() - propagates unrelated integrity violations")
    void register_ShouldPropagateUnrelatedIntegrityViolation() {
        when(userRepository.findByUsername("newUser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");

        DataIntegrityViolationException failure = new DataIntegrityViolationException("Unexpected integrity violation");

        when(userRepository.save(any(User.class))).thenThrow(failure);

        assertThatThrownBy(() -> userService.register("newUser", "new@test.com", "rawPassword")).isSameAs(failure);
    }
}
