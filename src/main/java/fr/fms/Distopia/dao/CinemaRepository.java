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
 * JPA repository used to manage {@link Cinema} entities
 * <p>
 * Provides CRUD operations as well as custom search
 * and filtering queries for public and administrative views
 */
@Repository
public interface CinemaRepository extends JpaRepository<Cinema,Long> {

    /**
     * Checks whether a cinema already exists
     * using its name and town name
     *
     * @param name the cinema name
     * @param townName the town name
     * @return true if the cinema already exists, otherwise false
     */
    boolean existsByNameAndTown_Name(String name, String townName);

    /**
     * Searches cinemas for the administration dashboard
     * <p>
     * Matches cinemas using cinema name or town name
     *
     * @param keyword the keyword used for the search
     * @param pageable the pagination configuration
     * @return a paginated list of matching cinemas
     */
    @Query("SELECT c FROM Cinema c LEFT JOIN c.town t " +
            "WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Cinema> searchAdmin(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Retrieves cinemas belonging to a specific town
     *
     * @param townId the town identifier
     * @param pageable the pagination configuration
     * @return a paginated list of cinemas in the given town
     */
    @Query("SELECT c FROM Cinema c WHERE c.town.id = :townId")
    Page<Cinema> findByTownId(@Param("townId") Long townId, Pageable pageable);

    /**
     * Searches cinemas visible to visitors
     * <p>
     * Matches cinemas using:
     * <ul>
     *     <li>cinema name</li>
     *     <li>cinema address</li>
     *     <li>town name</li>
     * </ul>
     *
     * @param k the keyword used for the search
     * @param pageable the pagination configuration
     * @return a paginated list of matching cinemas
     */
    @Query("SELECT c FROM Cinema c LEFT JOIN c.town t " +
            "WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :k, '%')) " +
            "OR LOWER(c.address) LIKE LOWER(CONCAT('%', :k, '%')) " +
            "OR LOWER(t.name) LIKE LOWER(CONCAT('%', :k, '%'))")
    Page<Cinema> searchPublic(@Param("k") String k, Pageable pageable);

    /**
     * Searches cinemas using both town and keyword filters
     *
     * @param townId the town identifier
     * @param k the keyword used for the search
     * @param pageable the pagination configuration
     * @return a paginated list of matching cinemas
     */
    @Query("SELECT c FROM Cinema c LEFT JOIN c.town t " +
            "WHERE c.town.id = :townId " +
            "AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :k, '%')) " +
            "OR LOWER(c.address) LIKE LOWER(CONCAT('%', :k, '%')))")
    Page<Cinema> searchByTownAndKeyword(@Param("townId") Long townId,
                                        @Param("k") String k,
                                        Pageable pageable);

    /**
     * Retrieves cinemas belonging to a department
     *
     * @param department the department code
     * @param pageable the pagination configuration
     * @return a paginated list of cinemas in the department
     */
    Page<Cinema> findByDepartment(String department, Pageable pageable);

    /**
     * Searches cinemas using both department and keyword filters
     *
     * @param dept the department code
     * @param k the keyword used for the search
     * @param pageable the pagination configuration
     * @return a paginated list of matching cinemas
     */
    @Query("SELECT c FROM Cinema c LEFT JOIN c.town t " +
            "WHERE c.department = :dept " +
            "AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :k, '%')) " +
            "OR LOWER(c.address) LIKE LOWER(CONCAT('%', :k, '%')) " +
            "OR LOWER(t.name) LIKE LOWER(CONCAT('%', :k, '%')))")
    Page<Cinema> searchByDepartmentAndKeyword(@Param("dept") String dept,
                                              @Param("k") String k,
                                              Pageable pageable);

    /**
     * Retrieves cinemas matching both town and department filters
     *
     * @param townId the town identifier
     * @param dept the department code
     * @param pageable the pagination configuration
     * @return a paginated list of matching cinemas
     */
    @Query("SELECT c FROM Cinema c " +
            "WHERE c.town.id = :townId AND c.department = :dept")
    Page<Cinema> findByTownIdAndDepartment(@Param("townId") Long townId,
                                           @Param("dept") String dept,
                                           Pageable pageable);

    /**
     * Retrieves all distinct cinema departments
     *
     * @return the list of unique department codes
     */
    @Query("SELECT DISTINCT c.department FROM Cinema c " +
            "WHERE c.department IS NOT NULL ORDER BY c.department")
    List<String> findDistinctDepartments();

    /**
     * Searches cinemas using town, department
     * and keyword filters simultaneously
     *
     * @param townId the town identifier
     * @param dept the department code
     * @param k the keyword used for the search
     * @param pageable the pagination configuration
     * @return a paginated list of matching cinemas
     */
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

