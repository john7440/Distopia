package fr.fms.Distopia.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Seance (screening) entity in the application
 * <p>
 * This class models a specific scheduled showtime for a movie. It holds
 * crucial business data such as the date and time of the screening, the ticket
 * price, the real-time count of available seats, and the cinema concerned
 * <p>
 * <strong>Note on Lombok:</strong> This class uses Lombok annotations
 * ({@code @Data}, {@code @AllArgsConstructor}, {@code @NoArgsConstructor})
 * to automatically generate getters, setters, and constructors
 */
@Entity
@Data
@AllArgsConstructor @NoArgsConstructor
public class Seance implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    @Min(value = 0, message = "Le nombre de places disponibles ne peut pas être négatif")
    @Max(value = 500, message = "Le nombre de places disponibles ne peut pas dépasser 500")
    private int availableSeats;

    @DecimalMin(value = "0.0", inclusive = false, message = "Le prix doit être supérieur à 0")
    private double price;

    private boolean active = true;

    @ManyToOne
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @OneToMany(mappedBy = "seance")
    private List<Reservation> reservations = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "cinema_id", nullable = false)
    private Cinema cinema;
}
