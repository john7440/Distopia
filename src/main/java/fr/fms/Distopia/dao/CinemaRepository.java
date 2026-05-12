package fr.fms.Distopia.dao;

import fr.fms.Distopia.entities.Cinema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The JPA Repository of cinemas
 */
@Repository
public interface CinemaRepository extends JpaRepository<Cinema,Long> {
    List<Cinema> findByTownId(Long townId);
    List<Cinema> findByNameContainingIgnoreCaseOrAddressContainingIgnoreCase(String name,String address);
    List<Cinema> findByTownIdAndNameContainingIgnoreCaseOrTownIdAndAddressContainingIgnoreCase(
            Long townId1, String name, Long townId2, String address);
    boolean existsByNameAndTown_Name(String name, String townName);
    @Query("SELECT c FROM Cinema c LEFT JOIN c.town t " +
            "WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Cinema> searchAdmin(@Param("keyword") String keyword, Pageable pageable);
}
