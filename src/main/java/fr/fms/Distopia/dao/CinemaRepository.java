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

    @Query("SELECT c FROM Cinema c LEFT JOIN c.town t WHERE c.town.id = :townId")
    Page<Cinema> findByTownId(@Param("townId") Long townId, Pageable pageable);

    @Query("SELECT c FROM Cinema c LEFT JOIN c.town t " +
            "WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :k, '%')) " +
            "OR LOWER(c.address) LIKE LOWER(CONCAT('%', :k, '%')) " +
            "OR LOWER(t.name) LIKE LOWER(CONCAT('%', :k, '%'))")
    Page<Cinema> searchPublic(@Param("k") String k, Pageable pageable);

    @Query("SELECT c FROM Cinema c LEFT JOIN c.town t " +
            "WHERE c.town.id = :townId " +
            "AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :k, '%')) " +
            "OR LOWER(c.address) LIKE LOWER(CONCAT('%', :k, '%')))")
    Page<Cinema> searchByTownAndKeyword(@Param("townId") Long townId,
                                        @Param("k") String k,
                                        Pageable pageable);

    Page<Cinema> findByDepartment(String department, Pageable pageable);

    @Query("SELECT c FROM Cinema c LEFT JOIN c.town t " +
            "WHERE c.department = :dept " +
            "AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :k, '%')) " +
            "OR LOWER(c.address) LIKE LOWER(CONCAT('%', :k, '%')) " +
            "OR LOWER(t.name) LIKE LOWER(CONCAT('%', :k, '%')))")
    Page<Cinema> searchByDepartmentAndKeyword(@Param("dept") String dept,
                                              @Param("k") String k,
                                              Pageable pageable);

    Page<Cinema> findByNameContainingIgnoreCaseAndDepartment(
            String keyword, String department, Pageable pageable);

    @Query("SELECT c FROM Cinema c " +
            "WHERE c.town.id = :townId AND c.department = :dept")
    Page<Cinema> findByTownIdAndDepartment(@Param("townId") Long townId,
                                           @Param("dept") String dept,
                                           Pageable pageable);

    @Query("SELECT DISTINCT c.department FROM Cinema c " +
            "WHERE c.department IS NOT NULL ORDER BY c.department")
    List<String> findDistinctDepartments();

    @Query("SELECT c FROM Cinema c LEFT JOIN c.town t " +
            "WHERE c.town.id = :townId " +
            "AND c.department = :dept " +
            "AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :k, '%')) " +
            "OR LOWER(c.address) LIKE LOWER(CONCAT('%', :k, '%')))")
    Page<Cinema> searchByAllFilters(@Param("townId") Long townId,
                                    @Param("dept") String dept,
                                    @Param("k") String k,
                                    Pageable pageable);
}

