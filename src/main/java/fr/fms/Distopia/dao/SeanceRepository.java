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
     * Archives all past seances by setting their {@code active} flag to {@code false}
     * <p>A seance is considered past if its {@code dateTime} is strictly before {@code now}
     * and it is still marked as active. Intended to be called by a scheduled task.
     *
     * @param now the reference timestamp - all seances before this value will be archived
     * @return the number of seances updated
     */
    @Modifying
    @Transactional
    @Query("""
        UPDATE Seance s
        SET s.active = false
        WHERE s.dateTime < :now
        AND s.active = true
        """)
    int archivePastSeances(@Param("now") LocalDateTime now);

    /**
     * Reactivates future seances that were previously archived
     *
     * @param now the current date and time used as activation threshold
     * @return the number of reactivated seances
     */
    @Modifying
    @Transactional
    @Query("""
        UPDATE Seance s
        SET s.active = true
        WHERE s.dateTime >= :now
        AND s.active = false
        """)
    int reactivateFutureSeances(@Param("now") LocalDateTime now);
}
