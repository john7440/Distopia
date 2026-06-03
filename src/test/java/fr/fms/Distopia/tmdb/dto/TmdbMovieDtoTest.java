package fr.fms.Distopia.tmdb.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TmdbMovieDtoTest {

    //--------------------tests for getReleaseYear()----------------------------
    @Test
    @DisplayName("getReleaseYear() - should return release year when release date is valid")
    void getReleaseYear_ShouldReturnReleaseYearWhenReleaseDateIsValid() {
        TmdbMovieDto movie = new TmdbMovieDto();
        movie.setReleaseDate("2026-05-26");

        String result = movie.getReleaseYear();

        assertThat(result).isEqualTo("2026");
    }

}
