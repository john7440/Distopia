package fr.fms.Distopia.dao;

import fr.fms.Distopia.entities.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie,Long> {
    List<Movie> findByCinemasIdAndDeletedFalse(Long cinemaId);
}
