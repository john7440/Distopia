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

import java.time.LocalDate;
import java.util.List;

@Controller
public class TmbdController {

    @Autowired
    private TmdbClient tmdbClient;

    @Autowired
    private MovieService movieService;

    @Autowired
    private SeanceGeneratorService seanceGeneratorService;

    // -----------------admin import-movies---------------------
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
    @PostMapping("admin/import-movie")
    public String importMovie(@RequestParam Long tmdbId,@RequestParam(required = false)String query, RedirectAttributes ra) {
        TmdbMovieDto detail = tmdbClient.getDetail(tmdbId);
        if (detail == null) {
            ra.addFlashAttribute("error", "Film introuvable sur TMDB (id=" + tmdbId + ")!");
            return "redirect:/admin/import-movies" + (query != null ? "?query=" + query : "");
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

        Movie saved = movieService.save(null, title, description, duration, genre, imageUrl, trailerUrl, null, releaseDate);

        SeanceGeneratorService.GeneratorResult result = seanceGeneratorService.generateForMovie(saved);

        ra.addFlashAttribute("message", String.format(
                "\"%s\" importé ! %d séances générées dans %d cinémas.",
                title, result.seancesCreated(),
                result.seancesCreated() / (7 * 3)
        ));
        return "redirect:/admin/import-movies" + (query != null ? "?query=" + query : "");

    }

    //-----------------------POST - import auto (de films à l'affiche) + génération de séances (fictives)-------------------------
    @PostMapping("/admin/generate-now-playing")
    public String generateNowPlaying(RedirectAttributes ra) {
        SeanceGeneratorService.GeneratorResult result = seanceGeneratorService.importAndGenerate();

        String message = String.format("%d films importés et %d séances générées sur 21 jours", result.moviesImported(), result.seancesCreated());
        ra.addFlashAttribute("message", message);

        if (result.hasErrors()){
            String errors = String.join(" | ", result.errors());
            ra.addFlashAttribute("warning" + "Avertissements" + errors);
        }
        return "redirect:/admin/seances";
    }
}
