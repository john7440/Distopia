package fr.fms.Distopia.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Movie entity in the application
 * <p>
 * This class holds the details of a film, such as its title, duration, and genre.
 * It also manages the relationships with the cinemas where it is screened and
 * its specific scheduled seances
 * <p>
 * <strong>Note on Lombok:</strong> This class uses Lombok annotations
 * ({@code @Getter},{@code @Setter}, {@code @AllArgsConstructor}, {@code @NoArgsConstructor})
 * to automatically generate getters, setters, and constructors
 */
@Entity
@Getter
@Setter
@AllArgsConstructor @NoArgsConstructor
public class Movie implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Positive(message = "L'identifiant TMDB doit être positif")
    @Column(unique = true)
    private Long tmdbId;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(min = 1, max = 150, message = "Le titre ne doit pas dépasser 150 caractères")
    @Column(nullable = false)
    private String title;

    @Size(max = 5000, message = "La description ne doit pas dépasser 5000 caractères")
    @Column(columnDefinition = "TEXT")
    private String description;

    @Min(value = 0, message = "La durée ne peut pas être négative")
    @Max(value = 600, message = "La durée ne peut pas dépasser 600 minutes")
    private int duration;

    @Size(max = 80, message = "Le genre ne doit pas dépasser 80 caractères")
    private String genre;

    @Column(nullable = false)
    private boolean deleted = false;

    private String imageUrl;

    private String trailerUrl;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @ManyToMany(mappedBy = "movies")
    private List<Cinema> cinemas =  new ArrayList<>();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL)
    private List<Seance> seances = new ArrayList<>();
}
