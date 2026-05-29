package fr.fms.Distopia.e2e;

import fr.fms.Distopia.dao.*;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class RegisterFlowSeleniumTest {

    @LocalServerPort
    private int port;
    @MockitoBean
    private TmdbClient tmdbClient;
    @Autowired
    private UserRepository  userRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private TownRepository townRepository;
    @Autowired
    private CinemaRepository cinemaRepository;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private SeanceRepository seanceRepository;

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void setUp() {
        when(tmdbClient.getNowPlaying()).thenReturn(List.of());
        when(tmdbClient.getThisWeek()).thenReturn(List.of());
        when(tmdbClient.getUpcoming()).thenReturn(List.of());

        clearDatabase();

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
    //------------------test for register flow ---------------------------
    @Test
    @DisplayName("register - should create user account from register modal")
    void register_ShouldCreateUserAccountFromRegisterModal() {
        driver.get("http://localhost:" + port + "/?openRegister");

        WebElement registerModal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("registerModal")));

        registerModal.findElement(By.name("username")).sendKeys("newuser");
        registerModal.findElement(By.name("email")).sendKeys("newuser@test.fr");
        registerModal.findElement(By.name("password")).sendKeys("password123");
        registerModal.findElement(By.cssSelector("form")).submit();

        wait.until(ExpectedConditions.urlContains("/"));

        assertThat(userRepository.findByUsername("newuser")).isPresent();
        assertThat(driver.getPageSource()).contains("Compte créé");
    }

    /**
     *  Used to clear the database easily before the test
     */
    private void clearDatabase() {
        reservationRepository.deleteAll();
        seanceRepository.deleteAll();
        movieRepository.deleteAll();
        cinemaRepository.deleteAll();
        townRepository.deleteAll();
        userRepository.deleteAll();
    }
}
