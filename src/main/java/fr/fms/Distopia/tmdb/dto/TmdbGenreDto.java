package fr.fms.Distopia.tmdb.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object representing a TMDB movie genre
 * <p>
 * Used to deserialize genre data returned by the TMDB API
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbGenreDto {

    private Long id;
    private String name;

}
