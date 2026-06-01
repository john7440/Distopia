package fr.fms.Distopia.dao;


import fr.fms.Distopia.entities.Seance;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA repository used to manage {@link Seance} entities
 * <p>
 * Provides CRUD operations as well as custom queries
 * for reservations, administration and upcoming seances
 */
@Repository
public interface SeanceRepository extends JpaRepository<Seance, Long> {

    //-------ajout d'un verrou pour éviter la race condition-------------
    /**
     * Retrieves a seance using a pessimistic write lock
     * <p>
     * This lock prevents concurrent modifications and helps
     * avoid race conditions during reservation processing
     *
     * @param id the seance identifier
     * @return an optional containing the locked seance if found
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seance s WHERE s.id = :id")
    Optional<Seance> findByIdForUpdate(@Param("id") Long id);

    /**
     * Retrieves all seances of a movie in a cinema
     * ordered by date and time
     *
     * @param movieId the movie identifier
     * @param cinemaId the cinema identifier
     * @return the list of matching seances sorted chronologically
     */
    List<Seance> findByMovieIdAndCinemaIdOrderByDateTimeAsc(Long movieId, Long cinemaId);

    /**
     * Retrieves upcoming seances for a movie
     * <p>
     * Only seances scheduled after the current timestamp
     * are returned
     * <p>
     * Cinema and movie entities are eagerly loaded using
     * {@link EntityGraph} to reduce lazy loading queries
     *
     * @param movieId the movie identifier
     * @param pageable the pagination configuration
     * @return a paginated list of upcoming seances
     */
    @EntityGraph(attributePaths = {"cinema", "movie"})
    @Query("""
        SELECT s
        FROM Seance s
        WHERE s.movie.id = :movieId
        AND s.dateTime >= CURRENT_TIMESTAMP
        ORDER BY s.cinema.id ASC, s.dateTime ASC
    """)
    Page<Seance> findUpcomingSeancesByMovie(
            @Param("movieId") Long movieId,
            Pageable pageable
    );

    /**
     * Searches seances for the administration dashboard
     * <p>
     * Supports:
     * <ul>
     *     <li>movie title keyword filtering</li>
     *     <li>cinema filtering</li>
     *     <li>pagination</li>
     * </ul>
     * <p>
     * Results are ordered chronologically
     *
     * @param keyword the keyword used to search movie titles
     * @param cinemaId the cinema identifier filter
     * @param pageable the pagination configuration
     * @return a paginated list of matching seances
     */
    @Query("""
    SELECT s FROM Seance s
    JOIN s.movie m
    JOIN s.cinema c
    WHERE (:keyword IS NULL OR :keyword = ''
           OR LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
    AND (:cinemaId IS NULL OR c.id = :cinemaId)
    """)
    Page<Seance> searchAdmin(@Param("keyword") String keyword, @Param("cinemaId") Long cinemaId, Pageable pageable);

    /**
     * Retrieves the IDs of seances matching the given admin filters
     *
     * <p>Filters are cumulative and optional:
     * <ul>
     *   <li>{@code keyword} — case-insensitive partial match on movie title or cinema name
     *       , ignored if {@code null} or empty</li>
     *   <li>{@code cinemaId} — exact match on the cinema ID, ignored if {@code null}</li>
     *   <li>{@code movieId} — exact match on the movie ID,ignored if {@code null}</li>
     * </ul>
     *
     * @param keyword  optional search term matched against movie title and cinema name
     * @param cinemaId optional ID of the cinema to filter by
     * @param movieId  optional ID of the movie to filter by
     * @return a list of seance IDs matching all provided filters, or all IDs if no filter is set
     */
    @Query("""
        SELECT s.id
        FROM Seance s
        WHERE (:keyword IS NULL OR :keyword = ''
               OR LOWER(s.movie.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(s.cinema.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:cinemaId IS NULL OR s.cinema.id = :cinemaId)
          AND (:movieId IS NULL OR s.movie.id = :movieId)
        """)
    List<Long> findIdsByAdminFilters(@Param("keyword") String keyword, @Param("cinemaId") Long cinemaId,
                                     @Param("movieId") Long movieId);
}
