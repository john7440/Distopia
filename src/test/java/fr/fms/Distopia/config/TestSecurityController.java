package fr.fms.Distopia.config;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test controller used only by SecurityConfigTest
 * <p>
 * It provides simple endpoints to verify security rules without depending on
 * application controllers or templates
 */
@Profile("security-test")
@RestController
public class TestSecurityController {

    @GetMapping("/cinemas")
    public ResponseEntity<String> cinemas() {
        return ResponseEntity.ok("cinemas");
    }

    @GetMapping("/index")
    public ResponseEntity<String> index() {
        return ResponseEntity.ok("index");
    }

    @GetMapping("/movies")
    public ResponseEntity<String> movies() {
        return ResponseEntity.ok("movies");
    }

    @GetMapping("/admin/cinemas")
    public ResponseEntity<String> adminCinemas() {
        return ResponseEntity.ok("admin cinemas");
    }

    @GetMapping("/my-reservations")
    public ResponseEntity<String> myReservations() {
        return ResponseEntity.ok("my reservations");
    }

    @PostMapping("/reserve")
    public ResponseEntity<String> reserve() {
        return ResponseEntity.ok("reserve");
    }
}