package fr.fms.Distopia.service;

import fr.fms.Distopia.dao.CinemaRepository;
import fr.fms.Distopia.dao.TownRepository;
import fr.fms.Distopia.entities.Town;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service responsible for town business operations.
 * <p>
 * It manages town listing, creation, edition and deletion with cinema
 * association cleanup.
 */
@Service
public class TownService {

    private final TownRepository townRepository;
    private final CinemaRepository cinemaRepository;

    public TownService(TownRepository townRepository,CinemaRepository cinemaRepository) {
        this.townRepository = townRepository;
        this.cinemaRepository = cinemaRepository;
    }

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
     * Creates a new town or updates an existing town
     *
     * <p>When the identifier is null, a new town is created.
     * When an identifier is provided, the corresponding town must already exist</p>
     *
     * <p>Unknown identifiers are rejected to prevent accidental creation of towns during update operations</p>
     *
     * @param id the town identifier, or null for creation
     * @param name the town name
     * @return the created or updated town
     * @throws java.util.NoSuchElementException if the provided identifier does not match an existing town
     */
    public Town save(Long id, String name) {
        Town town = (id != null) ? townRepository.findById(id).orElseThrow() : new Town();
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
