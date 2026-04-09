package fr.fms.Distopia.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Represents a User entity in the application
 * <p>
 * This class handles user account details, including authentication credentials
 * and role-based access control
 * <p>
 * <strong>Database Note:</strong> The {@code @Table(name="users")} annotation is used
 * intentionally because "user" is often a reserved keyword in many SQL databases.
 * Mapping it to "users" prevents SQL syntax errors
 * <p>
 * <strong>Note on Lombok:</strong> This class uses Lombok annotations
 * ({@code @Data}, {@code @AllArgsConstructor}, {@code @NoArgsConstructor})
 */
@Entity
@Table(name="users")
@Data
@AllArgsConstructor @NoArgsConstructor
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String username;
    @Column(nullable = false)
    private String password;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;
}
