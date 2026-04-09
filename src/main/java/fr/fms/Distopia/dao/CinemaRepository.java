package fr.fms.Distopia.dao;

import fr.fms.Distopia.entities.Cinema;
import org.springframework.data.jpa.repository.JpaRepository;
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

}
