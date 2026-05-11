package fr.fms.Distopia.dao;

import fr.fms.Distopia.entities.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The JPA Repository of movies
 */
@Repository
public interface MovieRepository extends JpaRepository<Movie,Long> {
    List<Movie> findByCinemasIdAndDeletedFalse(Long cinemaId);
    List<Movie> findByDeletedFalseOrderByTitleAsc();

    @Query("SELECT m FROM Movie m WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            " LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            " LOWER(m.genre) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Movie> searchAdmin(@Param("keyword") String keyword, Pageable pageable);
}
