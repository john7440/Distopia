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

}
