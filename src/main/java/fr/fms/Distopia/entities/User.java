package fr.fms.Distopia.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;


/**
 * Represents a User entity in the application
 * <p>
 * This class handles user account details, including authentication credentials,
 * email and role-based access control
 * <p>
 * <strong>Database Note:</strong> The {@code @Table(name="users")} annotation is used
 * intentionally because "user" is often a reserved keyword in many SQL databases.
 * Mapping it to "users" prevents SQL syntax errors
 * <p>
 * <strong>Note on Lombok:</strong> This class uses Lombok annotations
 * ({@code @Getter},{@code @Setter}, {@code @AllArgsConstructor}, {@code @NoArgsConstructor})
 */
@Entity
@Table(name="users")
@Getter
@Setter
@AllArgsConstructor @NoArgsConstructor
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    @Size(min = 3, max = 50, message = "Le nom d'utilisateur doit contenir entre 3 et 50 caractères")
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    @Size(max = 120, message = "L'email ne doit pas dépasser 120 caractères")
    @Column(nullable = false,unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    //-------------------ajout partie UserDetails----------------------------
    /**
     * Retourne les autorités de l'utilisateur
     * Spring Security attend le préfixe "ROLE_" : ROLE_ADMIN ou ROLE_USER
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
        return List.of(new SimpleGrantedAuthority("ROLE_"+ role.name()));
    }
}

