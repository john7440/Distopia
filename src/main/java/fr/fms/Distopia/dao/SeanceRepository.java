package fr.fms.Distopia.dao;


import fr.fms.Distopia.entities.Seance;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * The JPA Repository of seances
 */
@Repository
public interface SeanceRepository extends JpaRepository<Seance, Long> {
    List<Seance> findByMovieIdOrderByDateTimeAsc(Long movieId);
    //-------ajout d'un verrou pour éviter la race condition-------------
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seance s WHERE s.id = :id")
    Optional<Seance> findByIdForUpdate(@Param("id") Long id);
    List<Seance> findByMovieIdAndCinemaIdOrderByDateTimeAsc(Long movieId, Long cinemaId);
    List<Seance> findByMovieIdAndDateTimeAfterOrderByDateTimeAsc(Long movieId, LocalDateTime after);

    @Query("SELECT s FROM Seance s " +
            "JOIN s.movie m JOIN s.cinema c WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            " LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:cinemaId IS NULL OR c.id = :cinemaId) " +
            "ORDER BY s.dateTime ASC")
    Page<Seance> searchAdmin(@Param("keyword") String keyword, @Param("cinemaId") Long cinemaId, Pageable pageable);

}
