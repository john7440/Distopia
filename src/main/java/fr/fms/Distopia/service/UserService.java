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
    public Optional<User> login(@RequestParam String username, @RequestParam String rawPassword) {
        return userRepository.findByUsername(username).filter(user -> passwordEncoder.matches(rawPassword, user.getPassword()));
    }

    //-----------l'inscription----------------------------------
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
