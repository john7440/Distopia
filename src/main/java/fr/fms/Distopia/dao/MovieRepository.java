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
import java.util.Optional;

/**
 * JPA repository used to manage {@link Movie} entities
 * <p>
 * Provides CRUD operations as well as custom queries
 * for movie filtering, administration and TMDB integration
 */
@Repository
public interface MovieRepository extends JpaRepository<Movie,Long> {

    /**
     * Retrieves non-deleted movies associated with a cinema
     *
     * @param cinemaId the cinema identifier
     * @param sort the sorting configuration
     * @return the list of visible movies for the given cinema
     */
    List<Movie> findByCinemasIdAndDeletedFalse(Long cinemaId, Sort sort);

    /**
     * Retrieves all non-deleted movies
     *
     * @param sort the sorting configuration
     * @return the list of visible movies
     */
    List<Movie> findByDeletedFalse(Sort sort);

    /**
     * Searches a movie using its TMDB identifier
     *
     * @param tmdbId the TMDB movie identifier
     * @return an optional containing the matching movie if found
     */
    Optional<Movie> findByTmdbId(Long tmdbId);

    /**
     * Searches movies for the administration dashboard
     * <p>
     * Supports:
     * <ul>
     *     <li>keyword filtering on movie titles</li>
     *     <li>optional inclusion of deleted movies</li>
     *     <li>pagination</li>
     * </ul>
     * <p>
     * Cinemas are eagerly loaded using {@link EntityGraph}
     * to reduce lazy loading queries
     *
     * @param keyword the keyword used to search movie titles
     * @param showDeleted whether deleted movies should be included
     * @param pageable the pagination configuration
     * @return a paginated list of matching movies
     */
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
