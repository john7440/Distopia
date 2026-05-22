package fr.fms.Distopia.dao;


import fr.fms.Distopia.entities.Town;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA repository used to manage {@link Town} entities
 * <p>
 * Provides CRUD operations and lookup queries for towns
 */
@Repository
public interface TownRepository extends JpaRepository<Town,Long> {
    /**
     * Searches a town using its name
     * @param name the town name
     * @return an optional containing the matching town if found
     */
    Optional<Town> findByName(String name);
}
