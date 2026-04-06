package fr.fms.Distopia.dao;

import fr.fms.Distopia.entities.Cinema;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CinemaRepository extends JpaRepository<Cinema,Long> {
    List<Cinema> findByTownID(Long townId);
    List<Cinema> findByNameContainingIgnoreCaseOrAddressContainingIgnoreCase(String name,String address);

}
