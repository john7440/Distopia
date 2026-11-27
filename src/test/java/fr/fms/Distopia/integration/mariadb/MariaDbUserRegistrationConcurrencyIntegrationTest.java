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
     * Verifies that concurrent registrations using the same username result in exactly one persisted account
     */
    @Test
    @DisplayName("MariaDB - concurrent registrations cannot duplicate usernames")
    void registration_ShouldPreventConcurrentDuplicateUsername() throws InterruptedException, ExecutionException, TimeoutException {
        String username = "d05c-concurrent-user";
        String firstEmail = "d05c-first@example.test";
        String secondEmail = "d05c-second@example.test";
        String rawPassword = "test-only-password";

        User successfulUser = registerConcurrently(username, firstEmail, username, secondEmail, rawPassword);

        Long accountCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE username = ?", Long.class, username);

        assertThat(accountCount).isEqualTo(1L);

        User persistedUser = userRepository.findByUsername(username).orElseThrow();

        assertPersistedUser(persistedUser, successfulUser, rawPassword);

        String rejectedEmail = firstEmail.equals(successfulUser.getEmail()) ? secondEmail : firstEmail;

        assertThat(userRepository.findByEmail(rejectedEmail)).isEmpty();
    }


    /**
     * Verifies that concurrent registrations using the same email address result in exactly one persisted account
     *
     * <p>The rejected registration must not create another account with its requested username</p>
     */
    @Test
    @DisplayName("MariaDB - concurrent registrations cannot duplicate emails")
    void registration_ShouldPreventConcurrentDuplicateEmail() throws InterruptedException, ExecutionException, TimeoutException {

        String firstUsername = "d05d-first";
        String secondUsername = "d05d-second";
        String email = "d05d@example.test";
        String rawPassword = "test-only-password";

        User successfulUser = registerConcurrently(firstUsername, email, secondUsername, email, rawPassword);

        Long accountCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE email = ?", Long.class, email);

        assertThat(accountCount).isEqualTo(1L);
        User persistedUser = userRepository.findByEmail(email).orElseThrow();
        assertPersistedUser(persistedUser, successfulUser, rawPassword);
        String rejectedUsername = firstUsername.equals(successfulUser.getUsername()) ? secondUsername : firstUsername;

        assertThat(userRepository.findByUsername(rejectedUsername)).isEmpty();
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

    /**
     * Executes two concurrent registration requests
     *
     * <p>Exactly one registration must succeed, while the other must return an empty optional</p>
     *
     * @param firstUsername the first requested username
     * @param firstEmail the first requested email
     * @param secondUsername the second requested username
     * @param secondEmail the second requested email
     * @param rawPassword the password used by both requests
     * @return the successfully registered user
     */
    private User registerConcurrently(String firstUsername, String firstEmail, String secondUsername, String secondEmail,
                                      String rawPassword) throws InterruptedException, ExecutionException, TimeoutException {

        ExecutorService executor = Executors.newFixedThreadPool(2);
        boolean executorTerminated;
        User successfulUser;

        try {
            Future<Optional<User>> firstRegistration = executor.submit(() -> userService.register(firstUsername, firstEmail, rawPassword));
            Future<Optional<User>> secondRegistration = executor.submit(() -> userService.register(secondUsername, secondEmail, rawPassword));
            Optional<User> firstResult = firstRegistration.get(30, TimeUnit.SECONDS);
            Optional<User> secondResult = secondRegistration.get(30, TimeUnit.SECONDS);

            assertThat(firstResult.isPresent()).isNotEqualTo(secondResult.isPresent());
            successfulUser = firstResult.orElseGet(secondResult::orElseThrow);

        } finally {
            executor.shutdown();

            try {
                executorTerminated = executor.awaitTermination(20, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                executorTerminated = false;
            }
            if (!executorTerminated) {
                executor.shutdownNow();
            }
        }
        assertThat(executorTerminated).as("Concurrent registration tasks must terminate").isTrue();

        return successfulUser;
    }


    /**
     * Verifies the identity, role and password of a persisted user
     *
     * @param persistedUser the user retrieved from the database
     * @param successfulUser the successfully registered user
     * @param rawPassword the original registration password
     */
    private void assertPersistedUser(User persistedUser, User successfulUser, String rawPassword) {

        assertThat(persistedUser.getId()).isEqualTo(successfulUser.getId());
        assertThat(persistedUser.getUsername()).isEqualTo(successfulUser.getUsername());
        assertThat(persistedUser.getEmail()).isEqualTo(successfulUser.getEmail());
        assertThat(persistedUser.getRole()).isEqualTo(Role.USER);
        assertThat(passwordEncoder.matches(rawPassword, persistedUser.getPassword())).isTrue();
    }


}
