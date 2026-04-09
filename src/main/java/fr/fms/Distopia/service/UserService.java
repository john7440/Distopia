package fr.fms.Distopia.service;

import fr.fms.Distopia.dao.UserRepository;
import fr.fms.Distopia.entities.Role;
import fr.fms.Distopia.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    //-----------------le login------------------------------------
    /**
     * Authenticates a user by their username and raw password
     * <p>
     * This method retrieves the user by their username and verifies the provided
     * plain-text password against the securely hashed password stored in the database
     *
     * @param username    the username of the user attempting to log in
     * @param rawPassword the plain-text password provided by the user
     * @return an {@link Optional} containing the authenticated {@link User} if the
     * credentials are valid, or empty if the user is not found or the password does not match
     */
    public Optional<User> login(@RequestParam String username, @RequestParam String rawPassword) {
        return userRepository.findByUsername(username).filter(user -> passwordEncoder.matches(rawPassword, user.getPassword()));
    }

    //-----------l'inscription----------------------------------
    /**
     * Registers a new user with a default 'USER' role
     * <p>
     * This method first checks if the requested username is already taken. If it is,
     * the registration is aborted. Otherwise, it secures the provided password via
     * encoding, assigns the default {@link Role#USER}, and persists the new user to the database
     *
     * @param username    the desired username for the new account
     * @param rawPassword the plain-text password to be securely encoded and saved
     * @return an {@link Optional} containing the newly registered {@link User},
     * or empty if the username is already in use
     */
    public Optional<User> register(String username, String rawPassword) {
        if (userRepository.findByUsername(username).isPresent()) {
            return Optional.empty();
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(Role.USER);
        return Optional.of(userRepository.save(user));
    }

}
