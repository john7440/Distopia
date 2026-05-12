package fr.fms.Distopia.dao;

import fr.fms.Distopia.entities.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
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
    List<Movie> findByCinemasIdAndDeletedFalse(Long cinemaId, Sort sort);
    List<Movie> findByDeletedFalse(Sort sort);
    List<Movie> findByDeletedFalse();


    @EntityGraph(attributePaths = {"cinemas"})
    @Query(value = """
        SELECT DISTINCT m FROM Movie m
        WHERE (:keyword IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND   (:showDeleted = true OR m.deleted = false)
        """,
            countQuery = """
        SELECT COUNT(DISTINCT m) FROM Movie m
        WHERE (:keyword IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND   (:showDeleted = true OR m.deleted = false)
        """)
    Page<Movie> searchAdmin(@Param("keyword")String  keyword,
            @Param("showDeleted") boolean showDeleted, Pageable pageable
    );
}
