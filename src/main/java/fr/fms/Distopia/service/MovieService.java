package fr.fms.Distopia.service;

import fr.fms.Distopia.dao.CinemaRepository;
import fr.fms.Distopia.dao.MovieRepository;

import fr.fms.Distopia.dao.SeanceRepository;
import fr.fms.Distopia.entities.Movie;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MovieService {
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private CinemaRepository cinemaRepository;
    @Autowired
    private SeanceRepository seanceRepository;

    //-------les films d'un cinéma (sauf ceux supprimés)-------------
    /**
     * Retrieves a list of active movies available at a specific cinema
     * <p>
     * This method filters out movies that have been marked as deleted
     *
     * @param cinemaId the unique identifier of the cinema
     * @return a list of non-deleted {@link Movie} objects associated with the given cinema
     */
    public List<Movie> getByCinema(Long cinemaId) {
        return movieRepository.findByCinemasIdAndDeletedFalse(cinemaId);
    }

    //----------find by id----------------------
    /**
     * Retrieves a movie by its unique identifier
     *
     * @param id the unique identifier of the movie
     * @return an {@link Optional} containing the found {@link Movie}, or empty if no movie is found
     */
    public Optional<Movie> findById(Long id) {
        return movieRepository.findById(id);
    }

    //-----tous les films (même supprimés)-----------------
    /**
     * Retrieves all movies in the database
     * <p>
     * <strong>Note:</strong> This includes movies that have been marked as soft-deleted
     *
     * @return a list of all {@link Movie} objects
     */
    public List<Movie> getAll() {
        return movieRepository.findAll();
    }

    //--------------------créer ou modifier un film------------------
    /**
     * Creates a new movie or updates an existing one, along with its cinema associations
     * <p>
     * <strong>Association Handling:</strong> When updating, this method clears all existing
     * Many-To-Many relationships between the movie and its cinemas before establishing
     * the new ones provided in the {@code cinemaIds} list. This ensures the associations
     * are strictly synchronized with the provided input
     *
     * @param id          the unique identifier of the movie to update, or null to create a new one
     * @param title       the title of the movie
     * @param description the description or synopsis of the movie
     * @param duration    the duration of the movie in minutes
     * @param genre       the genre of the movie (e.g., Action, Comedy)
     * @param imageUrl    the URL to the movie's poster or cover image
     * @param cinemaIds   a list of cinema identifiers where the movie will be shown
     * @return the saved or updated {@link Movie} entity
     */
    @Transactional
    public Movie save(Long id, String title, String description,
                      int duration, String genre, String imageUrl,
                      List<Long> cinemaIds) {

        Movie movie = (id != null)
                ? movieRepository.findById(id).orElse(new Movie())
                : new Movie();

        movie.setTitle(title);
        movie.setDescription(description);
        movie.setDuration(duration);
        movie.setGenre(genre);
        movie.setImageUrl(imageUrl);

        movie.getCinemas().forEach(c -> c.getMovies().remove(movie));
        movie.getCinemas().clear();

        if (cinemaIds != null) {
            cinemaIds.forEach(cinemaId ->
                    cinemaRepository.findById(cinemaId).ifPresent(cinema -> {
                        cinema.getMovies().add(movie);
                        movie.getCinemas().add(cinema);
                    })
            );
        }
        return movieRepository.save(movie);
    }

    //----------------suppression d'un film (il reste en bdd) + désactivation des séances liées -----------
    /**
     * Performs a soft delete on a movie and disables its associated seances (screenings)
     * <p>
     * The movie is not physically removed from the database; instead, its {@code deleted}
     * flag is set to true. Furthermore, to prevent future bookings, the available seats
     * for all related seances are reduced to 0
     *
     * @param id the unique identifier of the movie to soft-delete
     */
    public void softDelete(Long id) {
        movieRepository.findById(id).ifPresent(movie -> {
            movie.setDeleted(true);
            movie.getSeances().forEach(seance -> seance.setAvailableSeats(0));
            seanceRepository.saveAll(movie.getSeances());
            movieRepository.save(movie);
        });
    }

}
