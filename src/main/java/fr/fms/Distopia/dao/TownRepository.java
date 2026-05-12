package fr.fms.Distopia.dao;


import fr.fms.Distopia.entities.Town;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * The JPA Repository of towns
 */
@Repository
public interface TownRepository extends JpaRepository<Town,Long> {
    Optional<Town> findByName(String name);
}
