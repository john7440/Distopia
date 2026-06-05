package fr.fms.Distopia.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Cinema entity in the application
 * <p>
 * This class is mapped to a database table and holds the core information
 * about a cinema, including its location (Town) and the catalog of movies
 * currently being screened there
 * <p>
 * <strong>Note on Lombok:</strong> This class uses Lombok annotations
 * ({@code @Data}, {@code @AllArgsConstructor}, {@code @NoArgsConstructor})
 * to automatically generate boilerplate code such as getters, setters,
 * and constructors
 */
@Entity
@Data
@AllArgsConstructor @NoArgsConstructor
public class Cinema implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom du cinéma est obligatoire")
    @Size(min = 2, max = 120, message = "Le nom doit contenir entre 2 et 120 caractères")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "L'adresse est obligatoire")
    @Size(max = 255, message = "L'adresse ne doit pas dépasser 255 caractères")
    private String address;

    @Size(max = 255, message = "L'Url du site web ne doit pas dépasser 255 caractères")
    private String website;

    @DecimalMin(value = "-90.0", message = "La latitude doit être supérieure ou égale à -90")
    @DecimalMax(value = "90.0", message = "La latitude doit être inférieure ou égale à 90")
    private Double latitude;

    @DecimalMin(value = "-180.0", message = "La longitude doit être supérieure ou égale à -180")
    @DecimalMax(value = "180.0", message = "La longitude doit être inférieure ou égale à 180")
    private Double longitude;

    @Size(max = 500, message = "L'URL de l'image ne doit pas dépasser 500 caractères")
    @Column
    private String imageUrl;

    @Pattern(regexp = "^$|^\\d{2,3}$", message = "Le département doit contenir 2 ou 3 chiffres")
    @Column(length = 3)
    private String department;

    private boolean deleted = false;

    @ManyToOne
    @JoinColumn(name = "town_id")
    private Town town;

    @ManyToMany
    @JoinTable(name = "cinema_movie",
            joinColumns = @JoinColumn(name = "cinema_id"),
            inverseJoinColumns = @JoinColumn(name = "movie_id"))
    private List<Movie> movies = new ArrayList<>();

    @OneToMany(mappedBy = "cinema")
    private List<Seance> seances = new ArrayList<>();

    /**
     * Builds a Google Maps URL for the cinema location
     * <p>
     * If latitude and longitude are available,
     * the method generates a direct Google Maps coordinates URL
     * <p>
     * Otherwise, if the address and town are available,
     * it generates a Google Maps search URL using the encoded address
     *
     * @return the Google Maps URL, or null if no location data is available
     */
    public String buildMapsUrl() {
        if (latitude != null && longitude != null)
            return "https://www.google.com/maps?q=" + latitude + "," + longitude;
        if (address != null && town != null)
            return "https://www.google.com/maps/search/?api=1&query="
                    + URLEncoder.encode(address + " " + town.getName(), StandardCharsets.UTF_8);
        return null;
    }

}
