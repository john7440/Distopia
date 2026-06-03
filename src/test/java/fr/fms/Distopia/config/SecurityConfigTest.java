package fr.fms.Distopia.config;

import fr.fms.Distopia.service.DistopiaUserDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TestSecurityController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("security-test")
@TestPropertySource(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration"
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @MockitoBean
    private DistopiaUserDetailsService userDetailsService;

    //*------------------tests public --------------------------
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
    @DisplayName("GET /movies - should be public")
    void movies_ShouldBePublic() throws Exception {
        mockMvc.perform(get("/movies")).andExpect(status().isOk());
    }

    //------------------tests Admin---------------------
    @Test
    @DisplayName("GET /admin/cinemas - should redirect anonymous user")
    void adminCinemas_ShouldRedirectAnonymousUser() throws Exception {
        mockMvc.perform(get("/admin/cinemas")).andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("GET /admin/cinemas - should forbid regular user")
    void adminCinemas_ShouldForbidRegularUser() throws Exception {
        mockMvc.perform(get("/admin/cinemas")
                        .with(user("user").roles("USER"))).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /admin/cinemas - should allow admin user")
    void adminCinemas_ShouldAllowAdminUser() throws Exception {
        mockMvc.perform(get("/admin/cinemas")
                        .with(user("admin").roles("ADMIN"))).andExpect(status().isOk());
    }

    //-------------------tests my-reservations  -------------------------
    @Test
    @DisplayName("GET /my-reservations - should redirect anonymous user")
    void myReservations_ShouldRedirectAnonymousUser() throws Exception {
        mockMvc.perform(get("/my-reservations")).andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("GET /my-reservations - accessible for authenticated user")
    void myReservations_ShouldBeAccessibleForAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/my-reservations")
                        .with(user("user").roles("USER"))).andExpect(status().isOk());
    }

    //------------------------ tests reserve---------------------------
    @Test
    @DisplayName("POST /reserve - should redirect anonymous user with valid csrf token")
    void reserve_ShouldRedirectAnonymousUserWithValidCsrfToken() throws Exception {
        mockMvc.perform(post("/reserve")
                        .with(csrf())).andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("POST /reserve - should be accessible for authenticated user with csrf token")
    void reserve_ShouldBeAccessibleForAuthenticatedUserWithCsrfToken() throws Exception {
        mockMvc.perform(post("/reserve")
                        .with(csrf())
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk());
    }

    //---------------------------tests csrf---------------------
    @Test
    @DisplayName("POST /reserve - should be forbidden without csrf token")
    void reserve_ShouldBeForbiddenWithoutCsrfToken() throws Exception {
        mockMvc.perform(post("/reserve")
                        .with(user("user").roles("USER"))).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /logout - should redirect to /index with csrf token")
    void logout_ShouldRedirectToIndexWithCsrfToken() throws Exception {
        mockMvc.perform(post("/logout")
                        .with(csrf())
                        .with(user("user").roles("USER")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index"));
    }

    @Test
    @DisplayName("POST /logout - should be forbidden without csrf token")
    void logout_ShouldBeForbiddenWithoutCsrfToken() throws Exception {
        mockMvc.perform(post("/logout")
                        .with(user("user").roles("USER"))).andExpect(status().isForbidden());
    }

    //------------------test PasswordEncoder---------------------
    @Test
    @DisplayName("passwordEncoder - encode and verify password")
    void passwordEncoder_ShouldEncodeAndVerifyPassword() {
        String raw = "Louvre";
        String encoded = passwordEncoder.encode(raw);

        assertThat(passwordEncoder.matches(raw, encoded)).isTrue();
        assertThat(passwordEncoder.matches("PasLouvre", encoded)).isFalse();
    }
}