package fr.fms.Distopia.integration.mariadb;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the dedicated MariaDB integration test environment
 *
 * <p>These tests require explicit activation and must never
 * use the development database or an embedded H2 database</p>
 */
@DataJpaTest
@ActiveProfiles("mariadb-test")
@TestPropertySource(locations = "classpath:application-mariadb-test.properties")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnabledIfSystemProperty(named = "distopia.mariadb.tests", matches = "true")
class MariaDbConnectionIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("MariaDB - connects to the isolated test database")
    void database_ShouldUseDedicatedMariaDbEnvironment() {
        String database = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);

        String version = jdbcTemplate.queryForObject("SELECT VERSION()", String.class);

        String currentUser = jdbcTemplate.queryForObject("SELECT CURRENT_USER()", String.class);

        Long userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);

        assertThat(database).isEqualTo("distopia_test");
        assertThat(version).containsIgnoringCase("MariaDB");
        assertThat(currentUser).isEqualTo("distopia_test@%");
        assertThat(userCount).isZero();
    }
}
