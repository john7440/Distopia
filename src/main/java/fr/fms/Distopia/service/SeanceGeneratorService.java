package fr.fms.Distopia.service;

import fr.fms.Distopia.entities.Cinema;
import fr.fms.Distopia.entities.Movie;
import fr.fms.Distopia.entities.Seance;
import fr.fms.Distopia.tmdb.TmdbClient;
import fr.fms.Distopia.tmdb.dto.TmdbMovieDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service responsible for importing movies from TMDB
 * and generating fictional seances<p>
 * This service:
 * <ul>
 *     <li>imports currently playing TMDB movies</li>
 *     <li>links movies to cinemas</li>
 *     <li>generates seances over several days</li>
 * </ul>
 */
@Service
public class SeanceGeneratorService {

    @Autowired private TmdbClient     tmdbClient;
    @Autowired private MovieService   movieService;
    @Autowired private SeanceService  seanceService;
    @Autowired private CinemaService  cinemaService;

    private static final int[]    HOURS  = {14, 17, 20};
    private static final int[]    MINS   = {0,  30, 45};
    private static final double[] PRICES = {9.00, 10.50, 12.00};


    // ----------------------- point d'entrée principal--------------------------

    /**
     * Imports currently playing movies and generates seances<p>
     * Main workflow:
     * <ol>
     *     <li>imports TMDB movies</li>
     *     <li>links movies to all cinemas</li>
     *     <li>generates fictional seances</li>
     * </ol>
     * @return the generation result containing statistics and errors
     */
    public GeneratorResult importAndGenerate() {
        List<String> errors = new ArrayList<>();

        List<Cinema> cinemas = cinemaService.getAll();
        if (cinemas.isEmpty()) {
            errors.add("Aucun cinéma en base, ajoutez des cinémas avant de générer des séances!");
            return new GeneratorResult(0, 0, errors);
        }

        int moviesImported = importMovies(errors);
        linkMoviesToCinemas(cinemas);
        int seancesCreated = generateSeances(cinemas, errors);

        return new GeneratorResult(moviesImported, seancesCreated, errors);
    }

    // -------étape 1 - import des films depuis TMDB ----------------------
    /**
     * Imports currently playing TMDB movies into the database
     * @param errors the shared error collection
     * @return the number of successfully imported movies
     */
    private int importMovies(List<String> errors) {
        List<TmdbMovieDto> nowPlaying = tmdbClient.getNowPlaying();
        if (nowPlaying.isEmpty()) {
            errors.add("TMDB n'a retourné aucun film ! (vérifier clé API)");
            return 0;
        }

        Set<String> existingTitles = movieService.getAllActive().stream()
                .map(m -> m.getTitle().toLowerCase())
                .collect(Collectors.toSet());

        int count = 0;
        for (TmdbMovieDto tmdbMovie : nowPlaying) {
            try {
                if (importSingleMovie(tmdbMovie, existingTitles)) count++;
            } catch (Exception e) {
                errors.add("Erreur import film ID " + tmdbMovie.getId() + " : " + e.getMessage());
            }
        }
        return count;
    }

    /**
     * Imports a single TMDB movie into the database<p>
     * Movies already existing locally are ignored
     * @param tmdbMovie the TMDB movie summary
     * @param existingTitles the set of existing local movie titles
     * @return true if the movie was imported, otherwise false
     */
    private boolean importSingleMovie(TmdbMovieDto tmdbMovie, Set<String> existingTitles) {
        TmdbMovieDto detail = tmdbClient.getDetail(tmdbMovie.getId());
        if (detail == null) return false;

        String title = detail.getTitle() != null ? detail.getTitle() : "Sans titre";
        if (existingTitles.contains(title.toLowerCase())) return false;

        movieService.save(
                null,
                tmdbMovie.getId(),
                title,
                detail.getOverview()  != null ? detail.getOverview() : "",
                detail.getRuntime()   != null ? detail.getRuntime()  : 90,
                extractGenre(detail),
                extractImageUrl(detail),
                tmdbClient.getTrailerUrl(tmdbMovie.getId()),
                null,
                parseReleaseDate(detail.getReleaseDate())
        );
        return true;
    }


    //------------étape 2 - association films/cinémas------------------------------------------
    /**
     * Associates movies without cinemas to all available cinemas
     *
     * @param cinemas the list of available cinemas
     */
    private void linkMoviesToCinemas(List<Cinema> cinemas) {
        List<Long> cinemaIds = cinemas.stream().map(Cinema::getId).toList();

        movieService.getAllActive().stream()
                .filter(m -> m.getCinemas() == null || m.getCinemas().isEmpty())
                .forEach(m -> movieService.save(
                        m.getId(), m.getTmdbId(), m.getTitle(), m.getDescription(),
                        m.getDuration(), m.getGenre(), m.getImageUrl(),
                        m.getTrailerUrl(), cinemaIds, m.getReleaseDate()
                ));
    }

    //----------------------------étape 3 - génération des séances---------------------
    /**
     * Generates fictional seances for all active movies and cinemas
     *
     * @param cinemas the list of cinemas
     * @param errors the shared error collection
     * @return the number of generated seances
     */
    private int generateSeances(List<Cinema> cinemas, List<String> errors) {
        List<Movie> movies = movieService.getAllActive();
        if (movies.isEmpty()) {
            errors.add("Aucun film actif en base après import");
            return 0;
        }

        LocalDateTime startDate = LocalDateTime.now().plusDays(1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);

        int count = 0;
        for (Movie movie : movies) {
            for (Cinema cinema : cinemas) {
                count += generateSeancesForMovieAndCinema(movie, cinema, startDate);
            }
        }
        return count;
    }

    /**
     * Generates fictional seances for a movie in a cinema
     * over seven days
     * <p>
     * Existing seances are ignored to avoid duplicates
     * @param movie the movie
     * @param cinema the cinema
     * @param startDate the generation start date
     * @return the number of generated seances
     */
    public int generateSeancesForMovieAndCinema(Movie movie, Cinema cinema,
                                                 LocalDateTime startDate) {
        Set<LocalDateTime> existing = seanceService
                .getByMovieAndCinema(movie.getId(), cinema.getId())
                .stream()
                .map(Seance::getDateTime)
                .collect(Collectors.toSet());

        int count = 0;
        for (int day = 0; day < 7; day++) {
            for (int slot = 0; slot < HOURS.length; slot++) {
                LocalDateTime dateTime = startDate.plusDays(day)
                        .withHour(HOURS[slot])
                        .withMinute(MINS[slot]);

                if (!existing.contains(dateTime)) {
                    seanceService.save(null, dateTime, 150, PRICES[slot],
                            movie.getId(), cinema.getId());
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Generates seances for a single movie
     * across all cinemas<p>
     * If the movie has no associated cinemas,
     * it is automatically linked to all cinemas
     *
     * @param movie the movie for which seances must be generated
     * @return the generation result containing statistics and errors
     */
    public GeneratorResult generateForMovie(Movie movie) {
        List<String> errors = new ArrayList<>();

        List<Cinema> cinemas = cinemaService.getAll();
        if (cinemas.isEmpty()) {
            errors.add("Aucun cinéma en base !");
            return new GeneratorResult(0, 0, errors);
        }

        List<Long> cinemaIds = cinemas.stream().map(Cinema::getId).toList();
        if (movie.getCinemas() == null || movie.getCinemas().isEmpty()) {
            movieService.save(
                    movie.getId(), movie.getTmdbId(), movie.getTitle(), movie.getDescription(),
                    movie.getDuration(), movie.getGenre(), movie.getImageUrl(),
                    movie.getTrailerUrl(), cinemaIds, movie.getReleaseDate()
            );
            movie = movieService.getById(movie.getId());
        }

        LocalDateTime startDate = LocalDateTime.now().plusDays(1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);

        int count = 0;
        for (Cinema cinema : cinemas) {
            count += generateSeancesForMovieAndCinema(movie, cinema, startDate);
        }

        return new GeneratorResult(1, count, errors);
    }

    //--------------------------méthodes helpers--------------------------------
    /**
     * Extracts the main genre of a TMDB movie
     *
     * @param detail the TMDB movie detail
     * @return the first genre name, or "Inconnu" if unavailable
     */
    private String extractGenre(TmdbMovieDto detail) {
        return (detail.getGenres() != null && !detail.getGenres().isEmpty())
                ? detail.getGenres().get(0).getName()
                : "Inconnu";
    }

    /**
     * Builds the full TMDB poster image URL
     * @param detail the TMDB movie detail
     * @return the full image URL, or null if unavailable
     */
    private String extractImageUrl(TmdbMovieDto detail) {
        return detail.getPosterPath() != null
                ? TmdbClient.IMG_BASE + detail.getPosterPath()
                : null;
    }

    /**
     * Parses a release date string
     * @param raw the raw release date string
     * @return the parsed release date, or null if invalid
     */
    private LocalDate parseReleaseDate(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try { return LocalDate.parse(raw); } catch (Exception e) { return null; }
    }

    //---------------------------résultat-----------------------------------------------
    /**
     * Generation result statistics
     *
     * @param moviesImported the number of imported movies
     * @param seancesCreated the number of generated seances
     * @param errors the list of generation errors
     */
    public record GeneratorResult(int moviesImported, int seancesCreated, List<String> errors) {
        /**
         * Indicates whether generation errors occurred
         * @return true if at least one error exists
         */
        public boolean hasErrors() {
            return !errors.isEmpty(); }
    }
}