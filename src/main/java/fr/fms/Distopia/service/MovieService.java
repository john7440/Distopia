package fr.fms.Distopia.service;

import fr.fms.Distopia.dao.CinemaRepository;
import fr.fms.Distopia.dao.MovieRepository;

import fr.fms.Distopia.dao.SeanceRepository;
import fr.fms.Distopia.entities.Movie;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service responsible for movie business operations.
 * <p>
 * It manages active/deleted movie queries, TMDB identifiers, cinema
 * associations, administrative search and soft deletion.
 */
@Service
public class MovieService {

    private final MovieRepository movieRepository;
    private final CinemaRepository cinemaRepository;
    private final SeanceRepository seanceRepository;
    private final SeanceService seanceService;

    public MovieService(MovieRepository movieRepository, CinemaRepository cinemaRepository, SeanceRepository seanceRepository,
                        SeanceService seanceService) {
        this.movieRepository = movieRepository;
        this.cinemaRepository = cinemaRepository;
        this.seanceRepository = seanceRepository;
        this.seanceService = seanceService;
    }
    private static final int PAGE_SIZE_ADMIN = 12;

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
     * Creates a new movie or updates an existing one,
     * along with its cinema associations
     * <p>
     * <strong>Association Handling:</strong>
     * When updating, all previous movie/cinema relations
     * are cleared before adding the new ones provided
     * in {@code cinemaIds}
     *
     * @param id          the movie identifier, or null for creation
     * @param tmdbId      the TMDB movie identifier
     * @param title       the movie title
     * @param description the movie synopsis
     * @param duration    the movie duration in minutes
     * @param genre       the movie genre
     * @param imageUrl    the movie poster URL
     * @param trailerUrl  the movie trailer URL
     * @param cinemaIds   the list of associated cinema identifiers
     * @param releaseDate the movie release date
     * @return the saved movie entity
     */
    @Transactional
    public Movie save(Long id,Long tmdbId, String title, String description,
                      int duration, String genre, String imageUrl, String trailerUrl,
                      List<Long> cinemaIds, LocalDate releaseDate) {

        Movie movie = (id != null)
                ? movieRepository.findById(id).orElse(new Movie())
                : new Movie();

        movie.setTitle(title);
        movie.setDescription(description);
        movie.setDuration(duration);
        movie.setGenre(genre);
        movie.setImageUrl(imageUrl);
        movie.setTrailerUrl(trailerUrl);
        movie.setReleaseDate(releaseDate);
        movie.setTmdbId(tmdbId);

        if (cinemaIds != null) {
            movie.getCinemas().forEach(c -> c.getMovies().remove(movie));
            movie.getCinemas().clear();

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

    //-------------chercher tous les films actifs---------------
    /**
     * Retrieves all active (non-deleted) movies
     * <p>
     * Movies are sorted alphabetically by title
     * @return the list of active movies
     */
    public List<Movie> getAllActive(){
        return movieRepository.findByDeletedFalse(Sort.by("title"));
    }

    /**
     * Retrieves all active (non-deleted) movies
     * using a custom sorting configuration
     * @param sort the sorting configuration
     * @return the list of active movies
     */
    public List<Movie> getAllActive(Sort sort) {
        return movieRepository.findByDeletedFalse(sort);
    }

    //---------------------pagination pour admin---------------------------------
    /**
     * Searches movies for the administration dashboard
     * <p>
     * Supports keyword search, deleted movie filtering,
     * pagination and dynamic sorting
     *
     * @param keyword     the keyword used to search movies
     * @param showDeleted whether deleted movies should be included
     * @param sortField   the field used for sorting
     * @param sortDir     the sorting direction (asc or desc)
     * @param page        the requested page number
     * @return a paginated list of movies
     */
    public Page<Movie> searchAdmin(@Param("keyword") String keyword,boolean showDeleted, String sortField,
                                   String sortDir,int page) {
        Sort sort = sortDir.equals("desc") ? Sort.by(sortField).descending() : Sort.by(sortField).ascending();
        Pageable pageable = PageRequest.of(page, PAGE_SIZE_ADMIN, sort);
        return movieRepository.searchAdmin(keyword, showDeleted,pageable);
    }

    /**
     * Retrieves a movie by its unique identifier
     *
     * @param id the unique identifier of the movie
     * @return the matching movie, or null if not found
     */
    public Movie getById(Long id) {
        return movieRepository.findById(id).orElse(null);
    }


    /**
     * Searches a movie using its TMDB identifier
     *
     * @param tmdbId the TMDB movie identifier
     * @return an optional containing the matching movie if found
     */
    public Optional<Movie> findByTmdbId(Long tmdbId) {
        return movieRepository.findByTmdbId(tmdbId);
    }

    /**
     * Returns active movies shown in a given cinema with at least one upcoming seance
     * <p>
     * Before searching, seance activity statuses are synchronized to avoid hiding
     * future seances that may have been archived incorrectly
     * @param cinemaId the cinema identifier
     * @param sort the sorting configuration
     * @return the list of active movies with upcoming seances for the selected cinema
     */
    public List<Movie> getByCinemaWithUpcomingSeances(Long cinemaId, Sort sort) {
        seanceService.syncSeanceActivityStatus();
        return movieRepository.findMoviesWithUpcomingSeancesByCinemaId(cinemaId, sort);
    }
}
