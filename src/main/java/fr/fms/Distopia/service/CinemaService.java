package fr.fms.Distopia.service;

import fr.fms.Distopia.dao.CinemaRepository;
import fr.fms.Distopia.dao.MovieRepository;
import fr.fms.Distopia.dao.TownRepository;
import fr.fms.Distopia.entities.Cinema;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class CinemaService {
    @Autowired
    private CinemaRepository cinemaRepository;
    @Autowired
    private TownRepository townRepository;
    @Autowired
    private MovieRepository movieRepository;


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
    public Page<Cinema> searchPublic(String keyword, Long townId, String department, int page) {
        Pageable pageable = PageRequest.of(page, 9, Sort.by("name").ascending());

        boolean hasK = keyword != null && !keyword.isBlank();
        boolean hasTown= townId != null;
        boolean hasDept = department != null && !department.isBlank();

        if (hasK && hasTown && hasDept)
            return cinemaRepository.searchByAllFilters(townId, department, keyword, pageable);
        if (hasK && hasTown)
            return cinemaRepository.searchByTownAndKeyword(townId, keyword, pageable);
        if (hasK && hasDept)
            return cinemaRepository.searchByDepartmentAndKeyword(department, keyword, pageable);
        if (hasTown && hasDept)
            return cinemaRepository.findByTownIdAndDepartment(townId, department, pageable);
        if (hasK)
            return cinemaRepository.searchPublic(keyword, pageable);
        if (hasTown)
            return cinemaRepository.findByTownId(townId, pageable);
        if (hasDept)
            return cinemaRepository.findByDepartment(department, pageable);

        return cinemaRepository.findAll(pageable);
    }

    //------------------recherche admin--------------------------
    public Page<Cinema> searchAdmin(String keyword, String sortField, String sortDir, int page) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortField);
        Pageable pageable = PageRequest.of(page, 12, sort);

        if (keyword != null && !keyword.isBlank()) {
            return cinemaRepository.searchAdmin(keyword, pageable);
        }
        return cinemaRepository.findAll(pageable);
    }

    //-----------afficher tous les cinémas-------------
    /**
     * Retrieves all available cinemas
     *
     * @return a list of all {@link Cinema} objects in the database
     */
    public List<Cinema> getAll(){
        return cinemaRepository.findAll().stream().filter(Objects::nonNull).toList();
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
    public Cinema save(Long id, String name, String address, Long townId, String website,
                       Double latitude, Double longitude,String imageUrl, String department) {
        Cinema cinema = (id != null) ? cinemaRepository.findById(id).orElse(new Cinema()): new Cinema();
        cinema.setName(name);
        cinema.setAddress(address);
        cinema.setWebsite(website);
        cinema.setLatitude(latitude);
        cinema.setLongitude(longitude);
        cinema.setImageUrl(imageUrl);
        cinema.setDepartment(department);
        if (townId != null){
            townRepository.findById(townId).ifPresent(cinema::setTown);
        }
        return cinemaRepository.save(cinema);
    }

    public List<String> getAllDepartments() {
        return cinemaRepository.findDistinctDepartments();
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
