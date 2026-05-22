package fr.fms.Distopia.dao;

import fr.fms.Distopia.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA repository used to manage {@link User} entities
 * <p>
 * Provides CRUD operations and authentication-related queries
 */
@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    /**
     * Searches a user using their username
     *
     * @param username the username used for authentication
     * @return an optional containing the matching user if found
     */
    Optional<User> findByUsername(String username);
}
