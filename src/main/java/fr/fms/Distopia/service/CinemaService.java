package fr.fms.Distopia.service;

import fr.fms.Distopia.dao.CinemaRepository;
import fr.fms.Distopia.dao.MovieRepository;
import fr.fms.Distopia.dao.TownRepository;
import fr.fms.Distopia.entities.Cinema;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CinemaService {
    @Autowired
    private CinemaRepository cinemaRepository;
    @Autowired
    private TownRepository townRepository;
    @Autowired
    private MovieRepository movieRepository;


    //------affichage cinéma d'une ville------------------
    /**
     * Retrieves a list of cinemas located in a specific town
     *
     * @param townId the unique identifier of the town
     * @return a list of {@link Cinema} objects belonging to the specified town
     */
    public List<Cinema> getByTown(Long townId) {
        return cinemaRepository.findByTownId(townId);
    }

    //-------find by id--------------
    /**
     * Retrieves a cinema by its unique identifier
     *
     * @param id the unique identifier of the cinema
     * @return an {@link Optional} containing the found {@link Cinema}, or empty if no cinema is found
     */
    public Optional<Cinema> findById(Long id) {
        return cinemaRepository.findById(id);
    }

    //-----------------rechercher par mot-clé (nom ou adresse)-------------------------
    /**
     * Searches for cinemas based on a keyword and/or a town identifier
     * <p>
     * The keyword is matched against both the cinema's name and address (case-insensitive)
     * The method adapts the search based on which parameters are provided:
     * <ul>
     * <li>If both keyword and townId are provided, it filters by both</li>
     * <li>If only the keyword is provided, it searches across all towns</li>
     * <li>If only the townId is provided, it returns all cinemas in that town</li>
     * <li>If neither are provided, it returns all cinemas</li>
     * </ul>
     *
     * @param keyword the search term to look for in the name or address (can be null or blank)
     * @param townId  the unique identifier of the town to filter by (can be null)
     * @return a list of {@link Cinema} objects matching the search criteria
     */
    public List<Cinema> search(String keyword, Long townId) {
        if (keyword != null && !keyword.isBlank() && townId != null) {
            return cinemaRepository
                    .findByTownIdAndNameContainingIgnoreCaseOrTownIdAndAddressContainingIgnoreCase(
                            townId, keyword, townId, keyword);
        } else if (keyword != null && !keyword.isBlank()) {
            return cinemaRepository
                    .findByNameContainingIgnoreCaseOrAddressContainingIgnoreCase(keyword, keyword);
        } else if (townId != null) {
            return cinemaRepository.findByTownId(townId);
        }
        return cinemaRepository.findAll();
    }

    //-----------afficher tous les cinémas-------------
    /**
     * Retrieves all available cinemas
     *
     * @return a list of all {@link Cinema} objects in the database
     */
    public List<Cinema> getAll(){
        return cinemaRepository.findAll();
    }

    //-----------------------créer ou modifier un cinéma----------------
    /**
     * Creates a new cinema or updates an existing one
     * <p>
     * If an ID is provided, the method attempts to fetch and update the existing cinema.
     * If the ID is null or the cinema is not found, a new {@link Cinema} instance is created
     *
     * @param id      the unique identifier of the cinema to update, or null to create a new one
     * @param name    the name of the cinema
     * @param address the address of the cinema
     * @param townId  the identifier of the town where the cinema is located
     * @return the saved or updated {@link Cinema} entity
     */
    public Cinema save(Long id, String name, String address, Long townId){
        Cinema cinema = (id != null) ? cinemaRepository.findById(id).orElse(new Cinema()): new Cinema();
        cinema.setName(name);
        cinema.setAddress(address);
        if (townId != null){
            townRepository.findById(townId).ifPresent(cinema::setTown);
        }
        return cinemaRepository.save(cinema);
    }

    //--------------supprimer un cinéma + vérification film orphelins----------------
    /**
     * Deletes a cinema by its unique identifier and handles orphaned movies
     * <p>
     * <strong>Note on Orphan Removal:</strong> Before deleting the cinema, this method iterates
     * through all associated movies and removes the cinema from their lists. If a movie
     * is no longer associated with any cinemas after this operation, it is marked as
     * deleted (soft delete) to avoid orphaned records
     *
     * @param id the unique identifier of the cinema to delete
     */
    @Transactional
    public void delete(Long id) {
        cinemaRepository.findById(id).ifPresent(cinema -> {
            cinema.getMovies().forEach(movie -> {
                movie.getCinemas().remove(cinema);
                if (movie.getCinemas().isEmpty()) {
                    movie.setDeleted(true);
                }
                movieRepository.save(movie);
            });
            cinemaRepository.delete(cinema);
        });
    }
}
