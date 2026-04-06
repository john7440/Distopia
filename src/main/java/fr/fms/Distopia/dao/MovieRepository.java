package fr.fms.Distopia.dao;

import fr.fms.Distopia.entities.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie,Long> {
}
