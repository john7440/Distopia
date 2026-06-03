package fr.fms.Distopia.service;

import fr.fms.Distopia.dao.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DistopiaUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private DistopiaUserDetailsService distopiaUserDetailsService;

    @Test
    @DisplayName("loadUserByUsername() - should throw UsernameNotFoundException when user does not exist")
    void loadUserByUsername_ShouldThrowUsernameNotFoundException_WhenUserDoesNotExist() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class, () -> distopiaUserDetailsService.loadUserByUsername("unknown"));

        assertThat(exception.getMessage()).isEqualTo("Utilisateur introuvable: unknown");
        verify(userRepository).findByUsername("unknown");
    }
}
