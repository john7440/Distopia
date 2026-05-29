package fr.fms.Distopia.e2e;

import fr.fms.Distopia.dao.UserRepository;
import fr.fms.Distopia.entities.Role;
import fr.fms.Distopia.entities.User;
import fr.fms.Distopia.tmdb.TmdbClient;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AdminNavigationSeleniumTest {

    @LocalServerPort
    private int port;
    @MockitoBean
    private TmdbClient tmdbClient;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void setUp() {
        when(tmdbClient.getNowPlaying()).thenReturn(List.of());
        when(tmdbClient.getThisWeek()).thenReturn(List.of());
        when(tmdbClient.getUpcoming()).thenReturn(List.of());

        userRepository.deleteAll();

        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@test.fr");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(Role.ADMIN);

        userRepository.save(admin);

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(5));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    //------------------tests for admin navigation ---------------------------
    @Test
    @DisplayName("admin - should access admin pages after login")
    void admin_ShouldAccessAdminPagesAfterLogin() {
        loginAsAdmin();

        goToAdminPageAndAssertTitle("/admin/movies", "Gestion des films");
        goToAdminPageAndAssertTitle("/admin/cinemas", "Gestion des cinémas");
        goToAdminPageAndAssertTitle("/admin/seances", "Gestion des séances");
        goToAdminPageAndAssertTitle("/admin/towns", "Gestion des villes");
    }

    private void goToAdminPageAndAssertTitle(String path, String expectedText) {
        driver.get(url(path));

        wait.until(ExpectedConditions.urlContains(path));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), expectedText));

        assertThat(driver.getPageSource()).contains(expectedText);
    }

    private void loginAsAdmin() {
        driver.get(url("/?openLogin"));

        WebElement loginModal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginModal"))
        );

        loginModal.findElement(By.name("username")).sendKeys("admin");
        loginModal.findElement(By.name("password")).sendKeys("admin123");

        loginModal.findElement(By.cssSelector("form")).submit();

        wait.until(ExpectedConditions.urlContains("/"));
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
