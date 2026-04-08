package fr.fms.Distopia.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor @NoArgsConstructor
public class Movie implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;
    private String description;
    private int duration;
    private String genre;
    @Column(nullable = false)
    private boolean deleted = false;
    private String imageUrl;

    @ManyToMany(mappedBy = "movies")
    private List<Cinema> cinemas =  new ArrayList<>();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL)
    private List<Seance> seances = new ArrayList<>();
}
