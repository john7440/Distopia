package fr.fms.Distopia.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;


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
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String username;
    @Column(nullable = false)
    private String password;
    @Column(unique = true)
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

    @Override
    public boolean isAccountNonExpired(){
        return true;
    }
    @Override
    public boolean isAccountNonLocked(){
        return true;
    }
    @Override
    public boolean isCredentialsNonExpired(){
        return true;
    }
    @Override
    public boolean isEnabled(){
        return true;
    }

}

