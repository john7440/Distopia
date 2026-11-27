package fr.fms.Distopia.service;

import fr.fms.Distopia.dao.UserRepository;
import fr.fms.Distopia.entities.Role;
import fr.fms.Distopia.entities.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;

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
     * Registers a new user with a default USER role
     *
     * <p>The username and email must be available before
     * registration. The password is encoded before persistence</p>
     *
     * <p>If a concurrent registration causes a database integrity violation, identifier availability is checked
     * again to determine whether a duplicate account exists</p>
     *
     * @param username the desired username
     * @param email the email address of the new account
     * @param rawPassword the plain-text password to encode
     * @return an optional containing the registered user, or empty if the username or email is already used
     * @throws DataIntegrityViolationException if an unrelated database integrity violation occurs
     */
    public Optional<User> register(
            String username, String email, String rawPassword) {

        if (isRegistrationIdentifierTaken(username, email)) {
            return Optional.empty();
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(Role.USER);

        try {
            return Optional.of(userRepository.save(user));
        } catch (DataIntegrityViolationException e) {
            if (isRegistrationIdentifierTaken(username, email)) {
                return Optional.empty();
            }
            throw e;
        }
    }

    /**
     * Checks whether a registration identifier is already used
     *
     * @param username the requested username
     * @param email the requested email address
     * @return true if either identifier is already registered
     */
    private boolean isRegistrationIdentifierTaken(String username, String email) {

        return userRepository.findByUsername(username).isPresent() || userRepository.findByEmail(email).isPresent();
    }
}
