package fr.fms.Distopia.service;

import fr.fms.Distopia.entities.Cinema;
import fr.fms.Distopia.entities.Movie;
import fr.fms.Distopia.tmdb.TmdbClient;
import fr.fms.Distopia.tmdb.dto.TmdbMovieDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service responsable de deux opérations combinées :
 *  1. Import des films TMDB "Now Playing" en base
 *  2. Génération automatique de séances futures pour chaque film x cinéma
 *
 * Logique de génération :
 *  - Chaque film est programmé dans chaque cinéma
 *  - 3 créneaux par jour : 14h, 17h30, 20h30
 *  - Sur 21 jours à partir de demain
 *  - Prix variant selon le créneau (matin < soir)
 *  - 80 places par défaut
 *
 * Le tout en évitant les doublons
 */
@Service
public class SeanceGeneratorService {

    @Autowired
    private TmdbClient tmdbClient;
    @Autowired
    private MovieService movieService;
    @Autowired
    private SeanceService seanceService;
    @Autowired
    private CinemaService cinemaService;

    //----------------schedule and price-------------------
    private static final int[] HOURS = {14,17,20};
    private static final int[] MINS = {0,30,45};
    private static final double[] PRICES = {9.00, 10.50, 12.00};

    public GeneratorResult importAndGenerate(){
        int moviesImported = 0;
        int seancesCreated = 0;
        List<String> errors = new ArrayList<>();

        List<TmdbMovieDto> nowPlaying = tmdbClient.getNowPlaying();
        if (nowPlaying.isEmpty()){
            errors.add("TMDB n'a retourné aucun films! (vérifier clé API)");
            return new GeneratorResult(0,0,errors);
        }

        for (TmdbMovieDto tmdbMovie : nowPlaying) {
            try {
                TmdbMovieDto detail = tmdbClient.getDetail(tmdbMovie.getId());
                if (detail == null) continue;

                String title    = detail.getTitle() != null ? detail.getTitle() : "Sans titre";
                String overview = detail.getOverview() != null ? detail.getOverview() : "";
                int    runtime  = detail.getRuntime() != null ? detail.getRuntime() : 90;
                String genre    = (detail.getGenres() != null && !detail.getGenres().isEmpty())
                        ? detail.getGenres().get(0).getName() : "Inconnu";
                String imageUrl = detail.getPosterPath() != null
                        ? TmdbClient.IMG_BASE + detail.getPosterPath() : null;
                String trailer  = tmdbClient.getTrailerUrl(tmdbMovie.getId());

                boolean alreadyExists = movieService.getAllActive().stream()
                        .anyMatch(m -> m.getTitle().equalsIgnoreCase(title));

                if (!alreadyExists) {
                    LocalDate releaseDate = null;
                    movieService.save(null, title, overview, runtime,
                            genre, imageUrl, trailer, null, releaseDate);
                    moviesImported++;
                }
            } catch (Exception e) {
                errors.add("Erreur import film ID: " + tmdbMovie.getId() + " : " + e.getMessage());
            }
        }

        List<Movie> movies = movieService.getAllActive();
        List<Cinema> cinemas = cinemaService.getAll();

        if (cinemas.isEmpty()){
            errors.add("Aucun cinéma en base, ajoutez des cinémas avant de générer des séances!");
            return new GeneratorResult(moviesImported,0,errors);
        }

        LocalDateTime startDate = LocalDateTime.now().plusDays(1)
                .withHour(0).withMinute(0).withSecond(0);

        for (Movie movie : movies) {
            for (Cinema cinema : cinemas) {
                for (int day = 0; day < 21; day++) {
                    for (int slot = 0; slot < 3; slot++) {
                        LocalDateTime dateTime = startDate.plusDays(day)
                                .withHour(HOURS[slot])
                                .withMinute(MINS[slot]);

                        boolean exists = seanceService
                                .getByMovieAndCinema(movie.getId(), cinema.getId())
                                .stream()
                                .anyMatch(s -> s.getDateTime().equals(dateTime));

                        if (!exists) {
                            seanceService.save(null, dateTime, 150, PRICES[slot],
                                    movie.getId(), cinema.getId());
                            seancesCreated++;
                        }
                    }
                }
            }
        }
        return new GeneratorResult(moviesImported, seancesCreated, errors);
    }

    public record GeneratorResult(int moviesImported, int seancesCreated, List<String> errors) {
        public boolean hasErrors() {
            return !errors.isEmpty(); }
    }
}
