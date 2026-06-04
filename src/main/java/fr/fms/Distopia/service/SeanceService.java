package fr.fms.Distopia.service;

import fr.fms.Distopia.dao.CinemaRepository;
import fr.fms.Distopia.dao.MovieRepository;
import fr.fms.Distopia.dao.ReservationRepository;
import fr.fms.Distopia.dao.SeanceRepository;
import fr.fms.Distopia.entities.Cinema;
import fr.fms.Distopia.entities.Movie;
import fr.fms.Distopia.entities.Seance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service responsible for seance business operations.
 * <p>
 * It manages seance retrieval, creation, deletion rules, administrative search
 * and upcoming seance pagination.
 */
@Service
public class SeanceService {

    private final SeanceRepository seanceRepository;
    private final MovieRepository movieRepository;
    private final CinemaRepository cinemaRepository;
    private final ReservationRepository reservationRepository;

    public SeanceService(SeanceRepository seanceRepository, MovieRepository movieRepository, CinemaRepository cinemaRepository,
                        ReservationRepository reservationRepository) {
        this.seanceRepository = seanceRepository;
        this.movieRepository = movieRepository;
        this.cinemaRepository = cinemaRepository;
        this.reservationRepository = reservationRepository;
    }

    private static final int PAGE_SIZE_ADMIN = 20;

    //---------------les séances d'un film---------------------
    /**
     * Retrieves upcoming active seances for a specific movie in a specific cinema<p>
     * This method is used by the public seance page. It only returns seances that
     * can still be booked: the seance must be active, scheduled in the future and
     * linked to a non-deleted movie
     *
     * @param movieId  the unique identifier of the movie
     * @param cinemaId the unique identifier of the cinema
     * @return a chronological list of available upcoming seances
     */
    public List<Seance> getByMovieAndCinema(Long movieId, Long cinemaId) {
        return seanceRepository.findUpcomingActiveByMovieAndCinema(
                movieId, cinemaId, LocalDateTime.now());
    }

    //--------------find by id------------
    /**
     * Retrieves a seance by its unique identifier
     *
     * @param id the unique identifier of the seance
     * @return an {@link Optional} containing the found {@link Seance}, or empty if no seance is found
     */
    public Optional<Seance> findById(Long id) {
        return seanceRepository.findById(id);
    }

    //-------------------créer ou modifier une séance-----------------------
    /**
     * Creates a new seance or updates an existing one
     * <p>
     * If an ID is provided, the method attempts to fetch and update the existing seance.
     * If the ID is null or the seance is not found, a new {@link Seance} instance is created
     *
     * @param id             the unique identifier of the seance to update, or null to create a new one
     * @param dateTime       the scheduled date and time of the screening
     * @param availableSeats the total number of seats available for this screening
     * @param price          the ticket price for this screening
     * @param movieId        the identifier of the movie being screened
     * @param cinemaId       the identifier of the cinema
     * @return the saved or updated {@link Seance} entity
     * @throws java.util.NoSuchElementException if a {@code movieId} is provided but the movie cannot be found
     */
    public Seance save(Long id, LocalDateTime dateTime, int availableSeats, double price, Long movieId, Long cinemaId) {
        Seance seance = (id!=null) ? seanceRepository.findById(id).orElse(new Seance()) : new Seance();

        seance.setDateTime(dateTime);
        seance.setActive(!dateTime.isBefore(LocalDateTime.now()));
        seance.setAvailableSeats(availableSeats);
        seance.setPrice(price);
        if(movieId !=null){
            Movie movie = movieRepository.findById(movieId).orElseThrow();
            seance.setMovie(movie);
        }
        Cinema cinema = cinemaRepository.findById(cinemaId).orElseThrow(() -> new RuntimeException("Cinéma introuvable"));
        seance.setCinema(cinema);
        return seanceRepository.save(seance);
    }

    //-----------supprimer une séance (ajout vérification de réservation)-------------
    /**
     * Deletes a seance by its unique identifier
     * <p>
     * <strong>Business Rule:</strong> This method enforces a strict constraint to prevent
     * the deletion of a seance if there are any existing reservations associated with it.
     * This ensures data integrity and prevents leaving users with orphaned bookings
     *
     * @param id the unique identifier of the seance to delete
     * @throws java.util.NoSuchElementException if the seance with the specified ID cannot be found
     * @throws IllegalStateException            if the seance has one or more associated reservations
     */
    @Transactional
    public void delete(Long id){
        Seance seance = seanceRepository.findById(id).orElseThrow();
        if (!seance.getReservations().isEmpty()){
            throw new IllegalStateException("Impossible de supprimer une séance avec des réservations");
        }
        seanceRepository.delete(seance);
    }

    //--------------recherche paginé admin avec tri et filtre-----------------
    /**
     * Searches seances for the administration page with pagination, sorting
     * and archive filtering<p>
     * Before searching, seance activity status are synchronized
     *
     * @param keyword the optional movie title keyword
     * @param cinemaId the optional cinema identifier filter
     * @param showArchived whether archived seances should be included
     * @param sortField the field used for sorting
     * @param sortDir the sorting direction, either "asc" or "desc"
     * @param page the requested page index
     * @return a paginated list of seances matching the filters
     */
    @Transactional
    public Page<Seance> searchAdmin(String keyword, Long cinemaId, boolean showArchived,String sortField,
                                    String sortDir, int page) {
        syncSeanceActivityStatus();

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortField).descending()
                : Sort.by(sortField).ascending();

        Pageable pageable = PageRequest.of(page, PAGE_SIZE_ADMIN, sort);

        return seanceRepository.searchAdmin(keyword, cinemaId, showArchived, pageable);
    }

    /**
     * Retrieves upcoming seances for a movie
     * <p>
     * This method is used on the public movie detail page. It excludes past seances,
     * inactive seances and seances linked to a soft-deleted movie
     *
     * @param movieId the movie identifier
     * @param page the requested page number
     * @param size the number of elements per page
     * @return a paginated list of upcoming seances
     */
    public Page<Seance> getUpcomingSeances(Long movieId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return seanceRepository.findUpcomingSeancesByMovie(movieId, pageable);
    }

    /**
     * Deletes selected seances by their identifiers
     *
     * @param ids the seance identifiers to delete
     * @return the number of deleted seances
     * @throws IllegalStateException if one or more selected seances have existing reservations
     */
    @Transactional
    public int deleteSelected(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        ensureSeancesHaveNoReservations(ids);
        seanceRepository.deleteAllByIdInBatch(ids);
        return ids.size();
    }

    /**
     * Deletes all seances matching the current admin filters<p>
     * @param keyword  the search keyword used in the admin page
     * @param cinemaId the selected cinema identifier, or null
     * @param showArchived whether archived seances are included in the deletion scope
     * @return the number of deleted seances
     * @throws IllegalStateException if one or more matching seances have existing reservations
     */
    @Transactional
    public int deleteByAdminFilters(String keyword, Long cinemaId, boolean showArchived) {
        syncSeanceActivityStatus();
        List<Long> ids = seanceRepository.findIdsByAdminFilters(keyword, cinemaId, showArchived);

        if (ids.isEmpty()) {
            return 0;
        }

        ensureSeancesHaveNoReservations(ids);
        seanceRepository.deleteAllByIdInBatch(ids);
        return ids.size();
    }

    /**
     * Ensures that none of the given seances are linked to existing reservations
     *
     * @param seanceIds the seance identifiers to check
     * @throws IllegalStateException if at least one seance has an existing reservation
     */
    private void ensureSeancesHaveNoReservations(List<Long> seanceIds) {
        if (seanceIds == null || seanceIds.isEmpty()) {
            return;
        }
        long reservationCount = reservationRepository.countBySeanceIds(seanceIds);
        if (reservationCount > 0) {
            throw new IllegalStateException(
                    "Impossible de supprimer une ou plusieurs séances car elles possèdent déjà des réservations"
            );
        }
    }

    /**
     * Synchronizes seance activity status according to the current date and time<p>
     * Past seances are archived and future seances are reactivated if needed
     */
    public void syncSeanceActivityStatus() {
        LocalDateTime now = LocalDateTime.now();

        seanceRepository.archivePastSeances(now);
        seanceRepository.reactivateFutureSeances(now);
    }
}
