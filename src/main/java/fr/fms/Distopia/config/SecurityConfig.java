package fr.fms.Distopia.config;

import fr.fms.Distopia.exceptions.SecurityFilterException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configures the security filter chain for the application
     * <p>
     * This configuration intentionally disables standard Spring Security protections:
     * <ul>
     * <li>Permits all incoming HTTP requests without authentication</li>
     * <li>Disables Cross-Site Request Forgery (CSRF) protection</li>
     * <li>Disables the default form-based login</li>
     * <li>Disables the default logout functionality</li>
     * </ul>
     * <strong>Warning:</strong> This is a completely permissive configuration. It should
     * typically only be used for development, testing environments, or specific public
     * APIs where security is handled at a different layer
     *
     * @param http the {@link HttpSecurity} builder used to configure web based security
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if an error occurs while building the security configuration
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws SecurityFilterException {
        http.authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll())
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
