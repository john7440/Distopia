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
import java.util.Objects;

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


    /**
     * Retrieves existing screenings for automatic generation
     *
     * <p>Unlike the public screening search, this method
     * includes inactive screenings. It prevents generated
     * screenings from replacing intentionally disabled ones</p>
     *
     * @param movieId the movie identifier
     * @param cinemaId the cinema identifier
     * @param start the inclusive start of the generation window
     * @param endExclusive the exclusive end of the generation window
     * @return screenings in the specified date range
     */
    @Transactional(readOnly = true)
    public List<Seance> getExistingForGeneration(Long movieId, Long cinemaId, LocalDateTime start, LocalDateTime endExclusive) {
        return seanceRepository.findExistingForGeneration(
                movieId, cinemaId, start, endExclusive);
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
     * Creates or updates a screening while enforcing reservation
     * integrity and screening lifecycle rules
     *
     * <p>Reservation integrity rules:</p>
     * <ul>
     *     <li>Available seats cannot be changed when reservations exist</li>
     *     <li>Screening date and time cannot be changed when reservations exist</li>
     *     <li>Ticket price cannot be changed when reservations exist  </li>
     *     <li>Associated movie cannot be changed when reservations exist</li>
     *     <li>Associated cinema cannot be changed when reservations exist</li>
     * </ul>
     *
     * <p>These checks are performed while holding a pessimistic write lock within the current transaction</p>

     * <p>Lifecycle rules:</p>
     * <ul>
     *     <li>New future screenings are active.</li>
     *     <li>Past screenings are inactive.</li>
     *     <li>Inactive future screenings remain inactive.</li>
     *     <li>Rescheduling a past screening to the future explicitly reactivates it</li>
     * </ul>
     *
     * @param id the screening ID, or null when creating a screening
     * @param dateTime the scheduled date and time
     * @param availableSeats the number of available seats
     * @param price the ticket price
     * @param movieId the movie ID
     * @param cinemaId the cinema ID
     * @return the saved screening
     * @throws java.util.NoSuchElementException if an existing screening or requested movie cannot be found
     * @throws IllegalStateException if reservation integrity or screening lifecycle rules are violated
     */
    @Transactional
    public Seance save(Long id, LocalDateTime dateTime, int availableSeats, double price, Long movieId, Long cinemaId) {

        Seance seance = (id != null) ? seanceRepository.findByIdForUpdate(id).orElseThrow() : new Seance();

        if (id != null && availableSeats != seance.getAvailableSeats() && reservationRepository.countBySeanceIds(List.of(id)) > 0) {
            throw new IllegalStateException("Impossible de modifier les places d'une séance ayant déjà des réservations");
        }

        Movie movie = (movieId != null) ? movieRepository.findById(movieId).orElseThrow() : seance.getMovie();

        if (movie != null && movie.isDeleted()) {
            throw new IllegalStateException("Impossible de programmer une séance pour un film supprimé");
        }

        Cinema cinema = cinemaRepository.findById(cinemaId).orElseThrow(() -> new RuntimeException("Cinéma introuvable"));

        if (cinema.isDeleted()) {
            throw new IllegalStateException("Impossible de programmer une séance dans un cinéma supprimé");
        }

        if (id != null && (!Objects.equals(seance.getMovie() != null ? seance.getMovie().getId() : null,
                movie != null ? movie.getId() : null) || !Objects.equals(seance.getCinema() != null ? seance.getCinema().getId() : null,
                cinema.getId())) && reservationRepository.countBySeanceIds(List.of(id)) > 0) {

            throw new IllegalStateException(
                    "Impossible de modifier le film ou le cinéma d'une séance ayant déjà des réservations");
        }


        if (id != null && (!Objects.equals(seance.getDateTime(), dateTime) || Double.compare(seance.getPrice(), price) != 0)
                && reservationRepository.countBySeanceIds(List.of(id)) > 0) {
            throw new IllegalStateException("Impossible de modifier la date ou le prix d'une séance ayant déjà des réservations");
        }

        LocalDateTime now = LocalDateTime.now();
        boolean future = !dateTime.isBefore(now);

        boolean rescheduledFromPast = id != null
                && seance.getDateTime() != null
                && seance.getDateTime().isBefore(now)
                && future;


        boolean active = future && (id == null || seance.isActive() || rescheduledFromPast);

        seance.setDateTime(dateTime);
        seance.setActive(active);
        seance.setAvailableSeats(availableSeats);
        seance.setPrice(price);

        if (movieId != null) {
            seance.setMovie(movie);
        }

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
            throw new IllegalStateException("Impossible de supprimer une ou plusieurs séances car elles possèdent déjà des réservations");
        }
    }

    /**
     * Synchronizes screening activity based on the current date
     *
     * <p>Past active screenings are automatically archived</p>
     *
     * <p>Future inactive screenings are never reactivated
     * automatically. Reactivation requires an explicit
     * rescheduling operation through the service</p>
     *
     * <p>This preserves intentional deactivation and prevents
     * previously archived screenings from becoming available
     * unexpectedly</p>
     */
    public void syncSeanceActivityStatus() {
        seanceRepository.archivePastSeances(LocalDateTime.now());
    }
}
