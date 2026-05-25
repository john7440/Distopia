package fr.fms.Distopia.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Data Transfer Object representing a TMDB search response
 * <p>
 * Used to deserialize paginated movie search results
 * returned by the TMDB API
 */
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbSearchResponse {

    private List<TmdbMovieDto> results;
}