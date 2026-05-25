package fr.fms.Distopia.service;

import fr.fms.Distopia.dao.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Custom Spring Security user details service
 * <p>
 * Responsible for loading application users from the database
 * during the authentication process
 */
@Service
public class DistopiaUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    /**
     * Loads a user using their username
     * <p>
     * This method is used by Spring Security during authentication
     * @param username the username used for authentication
     * @return the matching authenticated user details
     * @throws UsernameNotFoundException if no user matches the given username
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username).orElseThrow(()->
                new UsernameNotFoundException("Utilisateur introuvable: " + username));
    }
}
