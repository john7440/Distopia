package fr.fms.Distopia.config;

import fr.fms.Distopia.service.DistopiaUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private DistopiaUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF désactivé car nos formulaires Thymeleaf n'envoient pas encore de token CSRF!!!
                // À réactiver en production en ajoutant th:action dans chaque <form>
                .csrf(AbstractHttpConfigurer::disable)

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
                        .loginPage("/login")           // vue Thymeleaf
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/index", true)
                        .failureUrl("/login?error=true")
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

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
