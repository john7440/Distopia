package fr.fms.Distopia.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Town (or City) entity in the application
 * <p>
 * This class serves as a geographical grouping for cinemas. It allows the
 * application to filter and display cinemas based on their location
 * <p>
 * <strong>Note on Lombok:</strong> This class uses Lombok annotations
 * ({@code @Getter},{@code @Setter}, {@code @AllArgsConstructor}, {@code @NoArgsConstructor})
 * to automatically generate getters, setters, and constructors
 */
@Entity
@Getter
@Setter
@AllArgsConstructor @NoArgsConstructor
public class Town implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom de la ville est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom de la ville doit contenir entre 2 et 100 caractères")
    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "town")
    private List<Cinema> cinemas=  new ArrayList<>();
}
