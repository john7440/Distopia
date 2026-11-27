package fr.fms.Distopia.integration.mariadb;

import fr.fms.Distopia.dao.UserRepository;
import fr.fms.Distopia.entities.Role;
import fr.fms.Distopia.entities.User;
import fr.fms.Distopia.service.UserService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies concurrent user registration against MariaDB
 *
 * <p>Concurrent registrations must respect database uniqueness constraints and return an appropriate business result</p>
 */
@DataJpaTest
@Import({UserService.class, MariaDbUserRegistrationConcurrencyIntegrationTest.RegistrationTestConfiguration.class})
@ActiveProfiles("mariadb-test")
@TestPropertySource(locations = "classpath:application-mariadb-test.properties")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnabledIfSystemProperty(named = "distopia.mariadb.tests", matches = "true")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class MariaDbUserRegistrationConcurrencyIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Verifies that two concurrent registrations using the same username result in exactly one persisted account
     *
     * <p>Both requests pass the initial availability checks before attempting to persist their respective users</p>
     */
    @Test
    @DisplayName("MariaDB - concurrent registrations cannot duplicate usernames")
    void registration_ShouldPreventConcurrentDuplicateUsername()
            throws InterruptedException, ExecutionException, TimeoutException {

        String username = "d05c-concurrent-user";
        String firstEmail = "d05c-first@example.test";
        String secondEmail = "d05c-second@example.test";
        String rawPassword = "test-only-password";

        ExecutorService executor = Executors.newFixedThreadPool(2);
        boolean executorTerminated;

        try {
            Future<Optional<User>> firstRegistration = executor.submit(() -> userService.register(username, firstEmail, rawPassword));

            Future<Optional<User>> secondRegistration = executor.submit(() -> userService.register(username, secondEmail, rawPassword));

            Optional<User> firstResult = firstRegistration.get(30, TimeUnit.SECONDS);

            Optional<User> secondResult = secondRegistration.get(30, TimeUnit.SECONDS);

            assertThat(firstResult.isPresent()).isNotEqualTo(secondResult.isPresent());

            User successfulUser = firstResult.orElseGet(secondResult::orElseThrow);

            Long accountCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE username = ?", Long.class, username);

            assertThat(accountCount).isEqualTo(1L);

            User persistedUser = userRepository.findByUsername(username).orElseThrow();

            assertThat(persistedUser.getId()).isEqualTo(successfulUser.getId());
            assertThat(persistedUser.getEmail()).isEqualTo(successfulUser.getEmail());
            assertThat(persistedUser.getRole()).isEqualTo(Role.USER);
            assertThat(passwordEncoder.matches(rawPassword, persistedUser.getPassword())).isTrue();

            String rejectedEmail = firstResult.isPresent() ? secondEmail : firstEmail;

            assertThat(userRepository.findByEmail(rejectedEmail)).isEmpty();

        } finally {
            executor.shutdown();

            try {
                executorTerminated = executor.awaitTermination(
                        20, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                executorTerminated = false;
            }

            if (!executorTerminated) {
                executor.shutdownNow();
            }
        }

        assertThat(executorTerminated).as("Concurrent registration tasks must terminate").isTrue();
    }

    /**
     * Provides a password encoder dedicated to concurrency tests
     */
    @TestConfiguration(proxyBeanMethods = false)
    static class RegistrationTestConfiguration {
        @Bean
        PasswordEncoder passwordEncoder() {
            return new CoordinatedPasswordEncoder();
        }
    }

    /**
     * Performs real BCrypt password hashing and synchronizes
     * two registration requests before database insertion.
     */
    private static class CoordinatedPasswordEncoder extends BCryptPasswordEncoder {

        private final CyclicBarrier barrier = new CyclicBarrier(2);

        @Override
        public String encode(CharSequence rawPassword) {
            String encodedPassword = super.encode(rawPassword);

            try {
                barrier.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Registration synchronization interrupted", e);
            } catch (BrokenBarrierException | TimeoutException e) {
                throw new IllegalStateException("Registration synchronization failed", e);
            }

            return encodedPassword;
        }
    }
}
