package fr.fms.Distopia.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Data Transfer Object representing a TMDB movie
 * <p>
 * Used to deserialize movie data returned by the TMDB API
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbMovieDto {

    private Long id;
    private String title;
    private String overview;

    // JSON snake_case -> Java camelCase
    @JsonProperty("poster_path")
    private String posterPath;

    @JsonProperty("release_date")
    private String releaseDate;

    private Integer runtime;
    private List<TmdbGenreDto> genres;

    @JsonProperty("genre_ids")
    private List<Integer> genreIds;

    /**
     * Returns the release year extracted from the TMDB release date<p>
     * TMDB may return an empty string when the release date is unknown.
     * This method avoids StringIndexOutOfBoundsException in Thymeleaf templates.
     *
     * @return the release year, or "—" when unavailable
     */
    public String getReleaseYear() {
        if (releaseDate == null || releaseDate.length() < 4) {
            return "—";
        }
        return releaseDate.substring(0, 4);
    }

    /**
     * Returns a readable release date for display
     *
     * @return the formatted release date, or "A venir" when unavailable
     */
    public String getFormattedReleaseDate() {
        if (releaseDate == null || releaseDate.isBlank()) {
            return "A venir";
        }

        try {
            java.time.LocalDate date = java.time.LocalDate.parse(releaseDate);

            return "Sortie le " + date.format(
                    java.time.format.DateTimeFormatter.ofPattern(
                            "dd MMMM yyyy",
                            java.util.Locale.FRENCH));
        } catch (java.time.format.DateTimeParseException e) {
            return "A venir";
        }
    }

    /**
     * Returns a readable runtime for display
     *
     * @return the formatted runtime, or "Durée inconnue" when unavailable
     */
    public String getFormattedRuntime() {
        if (runtime == null || runtime <= 0) {
            return "Durée inconnue";
        }

        int hours = runtime / 60;
        int minutes = runtime % 60;

        if (hours == 0) {
            return minutes + "min";
        }
        if (minutes == 0) {
            return hours + "h";
        }
        return hours + "h " + minutes + "min";
    }

    /**
     * Returns the first available genre names for compact card display
     *
     * @return a list containing up to three genre names, or an empty list when unavailable
     */
    public List<String> getDisplayGenres() {
        if (genres == null || genres.isEmpty()) {
            return List.of();
        }

        return genres.stream().map(TmdbGenreDto::getName)
                .filter(name -> name != null && !name.isBlank())
                .limit(3).toList();
    }

}
