package fr.fms.Distopia.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Data Transfer Object representing a TMDB video response
 * <p>
 * Used to deserialize video search results
 * returned by the TMDB API
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbVideosResponse {

    private List<TmdbVideoDto> results;
}
