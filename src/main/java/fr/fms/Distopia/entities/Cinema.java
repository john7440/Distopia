package fr.fms.Distopia.entities;

import jakarta.persistence.*;
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
    @Column(nullable = false)
    private String name;
    private String address;
    private String website;
    private Double latitude;
    private Double longitude;

    @ManyToOne
    @JoinColumn(name = "town_id")
    private Town town;

    @ManyToMany
    @JoinTable(name = "cinema_movie",
            joinColumns = @JoinColumn(name = "cinema_id"),
            inverseJoinColumns = @JoinColumn(name = "movie_id"))
    private List<Movie> movies = new ArrayList<>();

    public String buildMapsUrl() {
        if (latitude != null && longitude != null)
            return "https://www.google.com/maps?q=" + latitude + "," + longitude;
        if (address != null && town != null)
            return "https://www.google.com/maps/search/?api=1&query="
                    + URLEncoder.encode(address + " " + town.getName(), StandardCharsets.UTF_8);
        return null;
    }

}
