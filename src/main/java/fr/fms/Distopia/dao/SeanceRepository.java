package fr.fms.Distopia.dao;


import fr.fms.Distopia.entities.Seance;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
     * Retrieves upcoming active seances for a specific movie in a specific cinema<p>
     * Only seances that are still active, scheduled after the provided date and linked
     * to a non-deleted movie are returned. Results are ordered chronologically so the
     * closest available screenings appear first
     *
     * @param movieId  the movie identifier
     * @param cinemaId the cinema identifier
     * @param now      the reference date and time used to exclude past seances
     * @return the list of available upcoming seances sorted chronologically
     */
    @EntityGraph(attributePaths = {"cinema", "movie"})
    @Query("""
    SELECT s
    FROM Seance s
    WHERE s.movie.id = :movieId
    AND s.cinema.id = :cinemaId
    AND s.active = true
    AND s.dateTime >= :now
    AND s.movie.deleted = false
    ORDER BY s.dateTime ASC
    """)
    List<Seance> findUpcomingActiveByMovieAndCinema(@Param("movieId") Long movieId, @Param("cinemaId") Long cinemaId,
            @Param("now") LocalDateTime now
    );


    /**
     * Retrieves existing screenings within the generation window
     *
     * <p>Includes both active and inactive screenings to prevent
     * automatic generation from recreating archived or cancelled
     * screening slots</p>
     *
     * @param movieId the movie identifier
     * @param cinemaId the cinema identifier
     * @param start the inclusive start of the generation window
     * @param endExclusive the exclusive end of the generation window
     * @return existing screenings regardless of activity status
     */
    @Query("""
    SELECT s FROM Seance s
    WHERE s.movie.id = :movieId
      AND s.cinema.id = :cinemaId
      AND s.dateTime >= :start
      AND s.dateTime < :endExclusive
    """)
    List<Seance> findExistingForGeneration(
            @Param("movieId") Long movieId,
            @Param("cinemaId") Long cinemaId,
            @Param("start") LocalDateTime start,
            @Param("endExclusive") LocalDateTime endExclusive);


    /**
     * Retrieves upcoming active seances for a movie
     * <p>
     * Only future seances that are still active and linked to a non-deleted movie
     * are returned. Cinema and movie entities are eagerly loaded using
     * {@link EntityGraph} to reduce lazy loading queries in the view layer
     *
     * @param movieId  the movie identifier
     * @param pageable the pagination configuration
     * @return a paginated list of available upcoming seances
     */
    @EntityGraph(attributePaths = {"cinema", "movie"})
    @Query("""
    SELECT s
    FROM Seance s
    WHERE s.movie.id = :movieId
    AND s.dateTime >= CURRENT_TIMESTAMP
    AND s.active = true
    AND s.movie.deleted = false
    ORDER BY s.cinema.id ASC, s.dateTime ASC
    """)
    Page<Seance> findUpcomingSeancesByMovie(@Param("movieId") Long movieId, Pageable pageable);

    /**
     * Searches seances with optional filters, returning a paginated result<p>
     * Filters are optional and cumulative:
     * <ul>
     *   <li>{@code keyword} - case-insensitive partial match on movie title,
     *       ignored if {@code null} or empty</li>
     *   <li>{@code cinemaId} -exact match on the cinema ID,ignored if {@code null}</li>
     *   <li>{@code showArchived} - if {@code false}, only active seances are returned</li>
     * </ul>
     *
     * @param keyword optional search term matched against movie title
     * @param cinemaId optional ID of the cinema to filter by
     * @param showArchived {@code true} to include archived seances, {@code false} for active only
     * @param pageable pagination and sorting parameters
     * @return a page of {@link Seance} matching all provided filters
     */
    @Query("""
        SELECT s FROM Seance s
        JOIN s.movie m
        JOIN s.cinema c
        WHERE (:keyword IS NULL OR :keyword = ''
               OR LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:cinemaId IS NULL OR c.id = :cinemaId)
        AND (:showArchived = true OR s.active = true)
        """)
    Page<Seance> searchAdmin(@Param("keyword") String keyword, @Param("cinemaId") Long cinemaId,
                             @Param("showArchived") boolean showArchived, Pageable pageable);

    /**
     * Retrieves the IDs of seances matching the given admin filters
     * <p>Applies the same filtering logic as {@link #searchAdmin} but returns
     * only IDs, typically used before a bulk delete operation
     *
     * @param keyword optional search term matched against movie title
     * @param cinemaId optional ID of the cinema to filter by
     * @param showArchived {@code true} to include archived seances, {@code false} for active only
     * @return a list of seance IDs matching all provided filters
     */
    @Query("""
        SELECT s.id FROM Seance s
        JOIN s.movie m
        JOIN s.cinema c
        WHERE (:keyword IS NULL OR :keyword = ''
               OR LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:cinemaId IS NULL OR c.id = :cinemaId)
        AND (:showArchived = true OR s.active = true)
        """)
    List<Long> findIdsByAdminFilters(@Param("keyword") String keyword, @Param("cinemaId") Long cinemaId,
                                     @Param("showArchived") boolean showArchived);

    /**
     * Archives active screenings scheduled before the given date.
     *
     * <p>The operation is idempotent: already inactive screenings remain unchanged</p>
     *
     * <p>Future screenings are not modified, regardless of their current activity status</p>
     *
     * @param now the reference date and time
     */
    @Modifying
    @Transactional
    @Query("""
        UPDATE Seance s
        SET s.active = false
        WHERE s.dateTime < :now
        AND s.active = true
        """)
    void archivePastSeances(@Param("now") LocalDateTime now);

    /**
    * Checks if at least one seance is linked to the given cinema<p>
    * @param cinemaId the unique identifier of the cinema to check
    * @return {@code true} if at least one seance exists for the cinema, otherwise {@code false}
    */
    boolean existsByCinemaId(Long cinemaId);
}
