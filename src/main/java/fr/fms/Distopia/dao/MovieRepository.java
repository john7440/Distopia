package fr.fms.Distopia.dao;

import fr.fms.Distopia.entities.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The JPA Repository of movies
 */
@Repository
public interface MovieRepository extends JpaRepository<Movie,Long> {
    List<Movie> findByCinemasIdAndDeletedFalse(Long cinemaId);
    List<Movie> findByDeletedFalseOrderByTitleAsc();
}
