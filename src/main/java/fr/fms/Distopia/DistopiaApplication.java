package fr.fms.Distopia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point of the Distopia Spring Boot application
 * <p>
 * This class bootstraps and launches the application
 * using Spring Boot
 */
@SpringBootApplication
public class DistopiaApplication {

    /**
     * Starts the Distopia application
     * @param args application startup arguments
     */
	public static void main(String[] args) {
		SpringApplication.run(DistopiaApplication.class, args);
	}
//-----comptes: -----
// - admin : admin123
// - alice: password
// - jonatemps : password
}
