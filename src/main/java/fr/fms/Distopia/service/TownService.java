package fr.fms.Distopia.service;

import fr.fms.Distopia.dao.CinemaRepository;
import fr.fms.Distopia.dao.TownRepository;
import fr.fms.Distopia.entities.Town;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TownService {

    @Autowired
    private TownRepository townRepository;

    @Autowired
    private CinemaRepository cinemaRepository;

    //----------------------methode pour trouver toutes les villes-----------------------
    /**
     * Retrieves all available towns
     *
     * @return a list of all {@link Town} objects in the database
     */
    public List<Town> getAll() {
        return townRepository.findAll();
    }

    //------------------méthode pour créer/modifier une ville----------------------------
    /**
     * Creates a new town or updates an existing one
     * <p>
     * If an ID is provided, the method attempts to fetch and update the existing town.
     * If the ID is null or the town is not found, a new {@link Town} instance is created
     *
     * @param id   the unique identifier of the town to update, or null to create a new one
     * @param name the name of the town
     * @return the saved or updated {@link Town} entity
     */
    public Town save(Long id, String name) {
        Town town = (id != null) ? townRepository.findById(id).orElse(new Town()) : new Town();
        town.setName(name);
        return townRepository.save(town);
    }

    //--------méthode pour supprimer une ville (avec modif de la ville des cinema impactés au besoin)------------
    /**
     * Deletes a town by its unique identifier and handles associated cinemas
     * <p>
     * <strong>Orphan Handling:</strong> Before the town is deleted, this method safely
     * unlinks all associated cinemas by setting their town reference to {@code null}.
     * This ensures that the cinemas remain in the database without triggering foreign
     * key constraint violations
     *
     * @param id the unique identifier of the town to delete
     */
    @Transactional
    public void delete(Long id) {
        townRepository.findById(id).ifPresent(town -> {
            town.getCinemas().forEach(cinema -> {
                cinema.setTown(null);
                cinemaRepository.save(cinema);
            });
            townRepository.delete(town);
        });
    }

}
