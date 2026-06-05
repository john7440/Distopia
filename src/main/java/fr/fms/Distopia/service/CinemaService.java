package fr.fms.Distopia.service;

import fr.fms.Distopia.dao.CinemaRepository;
import fr.fms.Distopia.dao.TownRepository;
import fr.fms.Distopia.entities.Cinema;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service responsible for cinema business operations.
 * <p>
 * It centralizes public and administrative searches, cinema persistence,
 * department listing and deletion rules.
 */
@Service
public class CinemaService {

    private final CinemaRepository cinemaRepository;
    private final TownRepository townRepository;

    public CinemaService(CinemaRepository cinemaRepository, TownRepository townRepository) {
        this.cinemaRepository = cinemaRepository;
        this.townRepository = townRepository;
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
     * Searches cinemas visible to visitors using optional filters
     * <p>
     * This method supports combinations of:
     * <ul>
     *     <li>keyword search (name or address)</li>
     *     <li>town filter</li>
     *     <li>department filter</li>
     * </ul>
     * Results are paginated and sorted alphabetically by cinema name
     *
     * @param keyword    the keyword used to search cinemas by name or address
     * @param townId     the identifier of the selected town
     * @param department the department code filter
     * @param page       the requested page number
     * @return a paginated list of matching cinemas
     */
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
            return cinemaRepository.findByDepartmentAndDeletedFalse(department, pageable);

        return cinemaRepository.findAll(pageable);
    }

    //------------------recherche admin--------------------------
    /**
     * Searches cinemas for the administration dashboard
     * <p>
     * Supports keyword search and dynamic sorting
     *
     * @param keyword   the keyword used to search cinemas
     * @param sortField the field used for sorting
     * @param sortDir   the sorting direction (asc or desc)
     * @param page      the requested page number
     * @return a paginated list of cinemas for administration
     */
    public Page<Cinema> searchAdmin(String keyword, String sortField, String sortDir, int page) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortField);
        Pageable pageable = PageRequest.of(page, 12, sort);

        if (keyword != null && !keyword.isBlank()) {
            return cinemaRepository.searchAdmin(keyword, pageable);
        }
        return cinemaRepository.findByDeletedFalse(pageable);
    }

    //-----------afficher tous les cinémas-------------
    /**
     * Retrieves all non-deleted cinemas
     *
     * @return a list of all {@link Cinema} objects in the database
     */
    public List<Cinema> getAll() {
        return cinemaRepository.findByDeletedFalse();
    }

    //-----------------------créer ou modifier un cinéma----------------
    /**
     * Creates a new cinema or updates an existing one
     * <p>
     * If an ID is provided, the method attempts to fetch and update the existing cinema.
     * If the ID is null or the cinema is not found, a new {@link Cinema} instance is created
     *
     * @param id the unique identifier of the cinema to update, or null to create a new one
     * @param name the name of the cinema
     * @param address the address of the cinema
     * @param townId the identifier of the town where the cinema is located
     * @param website the website URL
     * @param latitude the latitude of the cinema
     * @param longitude the longitude of the cinemas
     * @param imageUrl an image URL about the cinema
     * @param department the departement where the cinemas are located
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
        cinema.setDeleted(false);

        if (townId != null){
            townRepository.findById(townId).ifPresent(cinema::setTown);
        }
        return cinemaRepository.save(cinema);
    }

    /**
     * Retrieves all distinct cinema departments stored in the database
     *
     * @return a list of unique department codes
     */
    public List<String> getAllDepartments() {
        return cinemaRepository.findDistinctDepartments();
    }

    //--------------soft delete cinéma ----------------
    /**
     * Soft-deletes a cinema and disables its future seances
     * <p>
     * The cinema is not physically removed from the database. Its {@code deleted}
     * flag is set to {@code true}, which keeps reservation history consistent while
     * removing the cinema from public and administration listings.
     * <p>
     * Future seances linked to this cinema are marked as inactive and their
     * available seats are set to {@code 0}, preventing new reservations. Past
     * seances are kept unchanged to preserve historical data.
     *
     * @param id the unique identifier of the cinema to soft-delete
     */
    @Transactional
    public void delete(Long id) {
        cinemaRepository.findById(id).ifPresent(cinema -> {
            cinema.setDeleted(true);

            if (cinema.getSeances() != null) {
                cinema.getSeances().forEach(seance -> {
                    if (seance.getDateTime() != null && seance.getDateTime().isAfter(LocalDateTime.now())) {
                        seance.setActive(false);
                        seance.setAvailableSeats(0);
                    }
                });
            }
            cinemaRepository.save(cinema);
        });
    }
}
