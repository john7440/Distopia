package fr.fms.Distopia.config;

import fr.fms.Distopia.service.DistopiaUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration for the Distopia application
 * <p>
 * This configuration defines:
 * <ul>
 *     <li>authentication and authorization rules</li>
 *     <li>custom login and logout behavior</li>
 *     <li>password encoding strategy</li>
 * </ul>
 * <p>
 * Public pages remain accessible to visitors,
 * while administrative pages require the ADMIN role
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configures the Spring Security filter chain
     * <p>
     * Security rules:
     * <ul>
     *     <li>URLs under <code>/admin/**</code> require the ADMIN role</li>
     *     <li>reservation pages require authentication</li>
     *     <li>all other pages remain publicly accessible</li>
     * </ul>
     * <p>
     * This configuration also defines:
     * <ul>
     *     <li>a custom login page</li>
     *     <li>custom logout handling</li>
     *     <li>integration with the custom {@link DistopiaUserDetailsService}</li>
     * </ul>
     * <p>
     * Note: CSRF protection is temporarily disabled because Thymeleaf forms
     * do not yet include CSRF tokens !
     *
     * @param http the Spring Security HTTP configuration object
     * @param userDetailsService the custom user details service used for authentication
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if the security configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, DistopiaUserDetailsService userDetailsService) throws Exception {
        http
                .userDetailsService(userDetailsService)
                .authorizeHttpRequests(auth -> auth
                        // Accès admin uniquement
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Accès utilisateur connecté
                        .requestMatchers("/my-reservations", "/reserve").authenticated()

                        // Tout le reste : libre (visiteurs, pages publiques, assets)
                        .anyRequest().permitAll()
                )

                .formLogin(form -> form
                        .loginPage("/?openLogin")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/?loginError")
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/index")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        return http.build();
    }

    /**
     * Creates the password encoder used to hash user passwords
     * <p>
     * BCrypt is used because it provides strong password hashing
     *
     * @return the BCrypt password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
