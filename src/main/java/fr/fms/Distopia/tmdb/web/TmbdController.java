package fr.fms.Distopia.tmdb.web;

import fr.fms.Distopia.entities.Movie;
import fr.fms.Distopia.service.MovieService;
import fr.fms.Distopia.service.SeanceGeneratorService;
import fr.fms.Distopia.tmdb.TmdbClient;
import fr.fms.Distopia.tmdb.dto.TmdbMovieDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller responsible for TMDB movie import and TMDB-related
 * administration features
 * <p>
 * This controller allows administrators to:
 * <ul>
 *     <li>search movies from TMDB</li>
 *     <li>import TMDB movies into the local database</li>
 *     <li>generate fictional seances for imported movies</li>
 * </ul>
 */
@Controller
public class TmbdController {

    @Autowired
    private TmdbClient tmdbClient;

    @Autowired
    private MovieService movieService;

    @Autowired
    private SeanceGeneratorService seanceGeneratorService;

    // -----------------admin import-movies---------------------
    /**
     * Displays the TMDB movie import page
     * <p>
     * If a search query is provided, movies matching the query
     * are retrieved from TMDB and added to the model
     * @param query the TMDB search keyword
     * @param model the Spring {@link Model} used to pass data to the view
     * @return the TMDB administration import page
     */
    @GetMapping("/admin/import-movies")
    public String importPage(@RequestParam(required = false)String query, Model model) {
        if (query != null && !query.isBlank()) {
            List<TmdbMovieDto> results = tmdbClient.search(query);
            model.addAttribute("results", results);
            model.addAttribute("query", query);
        }
        model.addAttribute("imgBase", TmdbClient.IMG_BASE);
        return "admin-import-movies";
    }

    //--------------------importer film tmdb en bdd-------------------
    /**
     * Imports a TMDB movie into the local database
     * and generates fictional seances for it<p>
     * Imported data includes:
     * <ul>
     *     <li>title</li>
     *     <li>description</li>
     *     <li>runtime</li>
     *     <li>genre</li>
     *     <li>poster image</li>
     *     <li>trailer URL</li>
     *     <li>release date</li>
     * </ul><p>
     * If the movie cannot be retrieved from TMDB,
     * an error flash message is added
     * @param tmdbId the TMDB movie identifier
     * @param query the optional TMDB search keyword
     * @param ra the Spring {@link RedirectAttributes} used for flash messages
     * @return a redirect to the TMDB import administration page
     */
    @PostMapping("/admin/import-movie")
    public String importMovie(@RequestParam Long tmdbId,@RequestParam(required = false)String query, RedirectAttributes ra) {
        TmdbMovieDto detail = tmdbClient.getDetail(tmdbId);
        if (detail == null) {
            ra.addFlashAttribute("error", "Film introuvable sur TMDB (id=" + tmdbId + ")!");
            return redirectToImportMovies(query);
        }
        String title       = detail.getTitle() != null ? detail.getTitle() : "Sans titre";
        String description = detail.getOverview() != null ? detail.getOverview() : "";
        int    duration    = detail.getRuntime() != null ? detail.getRuntime() : 0;
        String genre       = (detail.getGenres() != null && !detail.getGenres().isEmpty())
                ? detail.getGenres().get(0).getName() : "Inconnu";
        String imageUrl    = detail.getPosterPath() != null
                ? TmdbClient.IMG_BASE + detail.getPosterPath() : null;
        String trailerUrl  = tmdbClient.getTrailerUrl(tmdbId);

        LocalDate releaseDate  = null;
        String raw = detail.getReleaseDate();
        if (raw != null && !raw.isBlank()) {
            try { releaseDate = LocalDate.parse(raw); } catch (Exception ignored) {}
        }

        Movie saved = movieService.save(null,tmdbId, title, description, duration, genre, imageUrl, trailerUrl, null, releaseDate);

        SeanceGeneratorService.GeneratorResult result = seanceGeneratorService.generateForMovie(saved);

        ra.addFlashAttribute("message", String.format(
                "\"%s\" importé ! %d séances générées dans %d cinémas.",
                title, result.seancesCreated(),
                result.seancesCreated() / (7 * 3)
        ));
        return redirectToImportMovies(query);

    }

    //-----------------------POST - import auto (de films à l'affiche) + génération de séances (fictives)-------------------------
    /**
     * Imports currently playing movies from TMDB and generates fake seances
     * <p>
     * This method imports now-playing movies, generates seances for them,
     * adds a success flash message, and optionally adds a warning message
     * when generation errors occur
     *
     * @param ra the Spring {@link RedirectAttributes} used to pass flash messages
     * @return a redirect to the admin seances page
     */
    @PostMapping("/admin/generate-now-playing")
    public String generateNowPlaying(RedirectAttributes ra) {
        SeanceGeneratorService.GeneratorResult result = seanceGeneratorService.importAndGenerate();

        String message = String.format("%d films importés et %d séances générées sur 21 jours", result.moviesImported(), result.seancesCreated());
        ra.addFlashAttribute("message", message);

        if (result.hasErrors()){
            String errors = String.join(" | ", result.errors());
            ra.addFlashAttribute("warning" , "Avertissements: " + errors);
        }
        return "redirect:/admin/seances";
    }

    /**
     * Builds the redirect URL to the TMDB import page<p>
     * If a query is provided, it is URL-encoded before being added
     * as a request parameter
     * @param query the optional search query
     * @return the redirect URL to the TMDB import page
     */
    private String redirectToImportMovies(String query) {
        if (query == null || query.isBlank()) {
            return "redirect:/admin/import-movies";
        }
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

        return "redirect:/admin/import-movies?query=" + encodedQuery;
    }
}
