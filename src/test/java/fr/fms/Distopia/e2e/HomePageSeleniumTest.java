package fr.fms.Distopia.e2e;


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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class HomePageSeleniumTest {

    @LocalServerPort
    private int port;

    @MockitoBean
    private TmdbClient tmdbClient;

    private WebDriver driver;

    @BeforeEach
    void setUp() {
        when(tmdbClient.getNowPlaying()).thenReturn(List.of());
        when(tmdbClient.getThisWeek()).thenReturn(List.of());
        when(tmdbClient.getUpcoming()).thenReturn(List.of());

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();

        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");

        driver = new ChromeDriver(options);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    //------------------tests for home page navigation ---------------------------
    @Test
    @DisplayName("home page - should navigate to movies page")
    void home_page_ShouldNavigateToMoviesPage() {
        driver.get("http://localhost:" + port + "/");

        WebElement title = driver.findElement(By.tagName("h1"));

        assertThat(title.getText()).contains("Distopia");

        WebElement moviesButton = driver.findElement(By.linkText("Voir tous les films"));
        moviesButton.click();

        assertThat(driver.getCurrentUrl()).contains("/movies");
    }
}
