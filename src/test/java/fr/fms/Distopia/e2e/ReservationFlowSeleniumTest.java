package fr.fms.Distopia.e2e;

import fr.fms.Distopia.dao.*;
import fr.fms.Distopia.entities.*;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ReservationFlowSeleniumTest {

    @LocalServerPort
    private int port;
    @MockitoBean
    private TmdbClient tmdbClient;
    @Autowired
    private UserRepository  userRepository;
    @Autowired
    private TownRepository townRepository;
    @Autowired
    private CinemaRepository cinemaRepository;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private SeanceRepository seanceRepository;
    @Autowired
    private PasswordEncoder  passwordEncoder;

    private WebDriver  driver;
    private WebDriverWait wait;

    private Movie  movie;

    @BeforeEach
    void setUp() {
        when(tmdbClient.getNowPlaying()).thenReturn(List.of());
        when(tmdbClient.getUpcoming()).thenReturn(List.of());

        clearDatabase();
        createTestData();

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
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

    //------------------test for reservation flow ---------------------------

    @Test
    @DisplayName("reservation - should create reservation when user is logged in")
    void reservation_ShouldCreateReservation_WhenUserIsLoggedIn() {
        loginAsUser();

        driver.get(url("/movie?id=" + movie.getId()));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"),"Inception"));

        WebElement reserveButton = wait.until(ExpectedConditions.elementToBeClickable(
                        By.cssSelector("form[action='/reserve'] button[type='submit']")));

        reserveButton.click();

        wait.until(ExpectedConditions.urlContains("/my-reservations"));

        assertThat(driver.getPageSource()).contains("Inception");
        assertThat(driver.getPageSource()).contains("bien enregistrée");
    }

    /**
     * Helper used to log in as a User
     */
    private void loginAsUser() {
        driver.get(url("/?openLogin"));

        WebElement loginModal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginModal")));

        loginModal.findElement(By.name("username")).sendKeys("user");
        loginModal.findElement(By.name("password")).sendKeys("user123");

        loginModal.findElement(By.cssSelector("form"))
                .submit();

        wait.until(ExpectedConditions.urlContains("/"));
    }

    /**
     *  Used to clear the database easily before the test
     */
    private void clearDatabase() {
        seanceRepository.deleteAll();
        movieRepository.deleteAll();
        cinemaRepository.deleteAll();
        townRepository.deleteAll();
        userRepository.deleteAll();
    }

    /**
     * Helper for generating Data
     */
    private void createTestData() {
        User user = new User();
        user.setUsername("user");
        user.setEmail("user@test.fr");
        user.setPassword(passwordEncoder.encode("user123"));
        user.setRole(Role.USER);
        userRepository.save(user);

        Town town = new Town();
        town.setName("Biarritz");
        town = townRepository.save(town);

        Cinema cinema = new Cinema();
        cinema.setName("Cinema Test");
        cinema.setAddress("1 rue du test");
        cinema.setTown(town);
        cinema.setDepartment("64");
        cinema = cinemaRepository.save(cinema);

        movie = new Movie();
        movie.setTitle("Inception");
        movie.setDescription("Dream movie");
        movie.setDuration(148);
        movie.setGenre("Science-fiction");
        movie.setImageUrl(null);
        movie.setTrailerUrl(null);
        movie.setReleaseDate(LocalDate.of(2010, 7, 21));
        movie.getCinemas().add(cinema);
        cinema.getMovies().add(movie);
        movie = movieRepository.save(movie);

        Seance seance = new Seance();
        seance.setMovie(movie);
        seance.setCinema(cinema);
        seance.setDateTime(LocalDateTime.now().plusDays(1));
        seance.setAvailableSeats(50);
        seance.setPrice(9.50);
        seanceRepository.save(seance);
    }

    /**
     * Helper to format the url with the (randomised) port and path
     * @param path the path to add to the url
     * @return the formated url
     */
    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
