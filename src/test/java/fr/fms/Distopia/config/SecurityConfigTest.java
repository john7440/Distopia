package fr.fms.Distopia.config;


import fr.fms.Distopia.service.*;
import fr.fms.Distopia.web.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CinemaController.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
@TestPropertySource(properties = {"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration"})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private CinemaService cinemaService;
    @MockitoBean
    private MovieService movieService;
    @MockitoBean
    private SeanceService seanceService;
    @MockitoBean
    private TownService townService;
    @MockitoBean
    private UserController userController;
    @MockitoBean
    private IndexController indexController;
    @MockitoBean
    private ReservationController reservationController;
    @MockitoBean
    private DistopiaUserDetailsService userDetailsService;

    //*------------------tests public -----------------------------
    @Test
    @DisplayName("GET /cinemas - should be public")
    void cinemas_ShouldBePublic() throws Exception {
        mockMvc.perform(get("/cinemas")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /index - should be public")
    void index_ShouldBePublic() throws Exception {
        mockMvc.perform(get("/index")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /login - should be public")
    void login_ShouldBePublic() throws Exception {
        mockMvc.perform(get("/login")).andExpect(status().isOk());
    }

    //------------------tests Admin-----------------------
    @Test
    @DisplayName("GET /admin/cinemas - should redirect anonymous user")
    void adminCinemas_ShouldRedirectAnonymouUser() throws Exception {
        mockMvc.perform(get("/admin/cinemas")).andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET admin/cinemas - should forbid regular user")
    void adminCinemas_ShouldForbidRegularUser() throws Exception {
        mockMvc.perform(get("/admin/cinemas")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET admin/cinemas - should allow admin user ")
    void adminCinemas_ShouldAllowAdminUser() throws Exception {
        mockMvc.perform(get("/admin/cinemas")).andExpect(status().isOk());
    }

    //------------------------tests my-reservations---------------------------
    @Test
    @DisplayName("GET /my-reservations - redirect to login if user not authenticated")
    void myReservations_shouldRedirectToLogin_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/my-reservations")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /my-reservations - accessible for authenticated user")
    void myReservations_ShouldBeAccessibleForAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/my-reservations")).andExpect(status().isOk());
    }

    //---------------------------test PasswordEncoder---------------------
    @Test
    @DisplayName("passwordEncoder - encode and verify password")
    void passwordEncoder_ShouldEncodeAndVerifyPassword() throws Exception {
        PasswordEncoder encoder =  new BCryptPasswordEncoder();
        String raw = "Louvre";
        String encoded = encoder.encode(raw);

        assertThat(encoder.matches(raw, encoded)).isTrue();
        assertThat(encoder.matches("PasLouvre", encoded)).isFalse();

    }
}
