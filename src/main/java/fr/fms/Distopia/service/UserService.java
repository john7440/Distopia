package fr.fms.Distopia.service;

import fr.fms.Distopia.dao.UserRepository;
import fr.fms.Distopia.entities.Role;
import fr.fms.Distopia.entities.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service responsible for user account operations.
 * <p>
 * It handles registration and password verification while keeping password
 * encoding logic outside the web controllers.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    //-----------l'inscription----------------------------------
    /**
     * Registers a new user with a default 'USER' role.
     * <p>
     * This method first checks if the requested username or email is already taken.
     * If one of them is already used, the registration is aborted. Otherwise, it
     * secures the provided password via encoding, assigns the default {@link Role#USER},
     * and persists the new user to the database.
     *
     * @param username    the desired username for the new account
     * @param email       the email address for the new account
     * @param rawPassword the plain-text password to be securely encoded and saved
     * @return an {@link Optional} containing the newly registered {@link User},
     * or empty if the username or email is already in use
     */
    public Optional<User> register(String username, String email, String rawPassword) {
        if (userRepository.findByUsername(username).isPresent()
                || userRepository.findByEmail(email).isPresent()) {
            return Optional.empty();
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(Role.USER);

        return Optional.of(userRepository.save(user));
    }
}
