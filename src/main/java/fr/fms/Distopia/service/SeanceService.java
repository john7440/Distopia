package fr.fms.Distopia.service;

import fr.fms.Distopia.dao.CinemaRepository;
import fr.fms.Distopia.dao.MovieRepository;
import fr.fms.Distopia.dao.SeanceRepository;
import fr.fms.Distopia.entities.Movie;
import fr.fms.Distopia.entities.Seance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SeanceService {
    @Autowired
    private SeanceRepository seanceRepository;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private CinemaRepository cinemaRepository;

    //---------------les séances d'un film---------------------
    /**
     * Retrieves all scheduled seances for a specific movie in a specific cinema
     * <p>
     * The results are ordered chronologically by their date and time in ascending order
     * (the earliest screenings are returned first)
     *
     * @param movieId the unique identifier of the movie
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
        cinemaRepository.findById(cinemaId).ifPresent(seance::setCinema);
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
}
