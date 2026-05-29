package fr.fms.Distopia.service;

import fr.fms.Distopia.dao.CinemaRepository;
import fr.fms.Distopia.dao.MovieRepository;
import fr.fms.Distopia.dao.SeanceRepository;
import fr.fms.Distopia.entities.Cinema;
import fr.fms.Distopia.entities.Movie;
import fr.fms.Distopia.entities.Seance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SeanceService {

    private final SeanceRepository seanceRepository;
    private final MovieRepository movieRepository;
    private final CinemaRepository cinemaRepository;

    public SeanceService(SeanceRepository seanceRepository, MovieRepository movieRepository, CinemaRepository cinemaRepository) {
        this.seanceRepository = seanceRepository;
        this.movieRepository = movieRepository;
        this.cinemaRepository = cinemaRepository;
    }

    private static final int PAGE_SIZE_ADMIN = 20;

    //---------------les séances d'un film---------------------
    /**
     * Retrieves all scheduled seances for a specific movie in a specific cinema
     * <p>
     * The results are ordered chronologically by their date and time in ascending order
     * (the earliest screenings are returned first)
     *
     * @param movieId the unique identifier of the movie
     * @param cinemaId the identifier of the cinema
     * @return a list of {@link Seance} objects scheduled for the specified movie
     */
    public List<Seance> getByMovieAndCinema(Long movieId, Long cinemaId) {
        return seanceRepository.findByMovieIdAndCinemaIdOrderByDateTimeAsc(movieId, cinemaId);
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

    //------------toutes les séances-------------
    /**
     * Retrieves all seances available in the database.
     *
     * @return a list of all {@link Seance} objects
     */
    public List<Seance> getAll(){
        return seanceRepository.findAll();
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
    public void delete(Long id){
        Seance seance = seanceRepository.findById(id).orElseThrow();
        if (!seance.getReservations().isEmpty()){
            throw new IllegalStateException("Impossible de supprimer une séance avec des réservations");
        }
        seanceRepository.delete(seance);
    }

    //--------------recherche paginé admin-----------------
    /**
     * Searches seances for the administration page with pagination and sorting
     *
     * @param keyword the optional movie title keyword
     * @param cinemaId the optional cinema identifier filter
     * @param sortField the field used for sorting
     * @param sortDir the sorting direction, either "asc" or "desc"
     * @param page the requested page index
     * @return a paginated list of seances matching the filters
     */
    public Page<Seance> searchAdmin(String keyword, Long cinemaId, String sortField,
                                    String sortDir, int page) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortField).descending()
                : Sort.by(sortField).ascending();

        Pageable pageable = PageRequest.of(page, PAGE_SIZE_ADMIN, sort);

        return seanceRepository.searchAdmin(keyword, cinemaId, pageable);
    }

    /**
     * Retrieves upcoming seances for a movie
     * <p>
     * Only future seances associated with the given movie
     * are returned
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
}
