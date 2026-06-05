package fr.fms.Distopia.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents a Reservation entity in the application
 * <p>
 * This class captures the details of a booking made by a user for a specific
 * movie screening (seance). It tracks when the reservation was made, who made it,
 * which screening it is for, and how many seats were booked
 * <p>
 * <strong>Note on Lombok:</strong> This class uses Lombok annotations
 * ({@code @Getter},{@code @Setter}, {@code @AllArgsConstructor}, {@code @NoArgsConstructor})
 * to automatically generate getters, setters, and constructors
 */
@Entity
@Getter
@Setter
@AllArgsConstructor @NoArgsConstructor
public class Reservation implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime reservedAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "seance_id", nullable = false)
    private Seance seance;

    @Min(value = 1, message = "La quantité doit être au moins de 1")
    @Column(nullable = false)
    private int quantity;

}
