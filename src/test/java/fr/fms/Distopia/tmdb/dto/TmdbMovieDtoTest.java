package fr.fms.Distopia.tmdb.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;


import java.util.stream.Stream;

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

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"20"})
    @DisplayName("getReleaseYear() - should return dash when release date is null or too short")
    void getReleaseYear_ShouldReturnDashWhenReleaseDateIsNull(String ignoredInput) {
        TmdbMovieDto movie = new TmdbMovieDto();
        movie.setReleaseDate(null);

        String result = movie.getReleaseYear();

        assertThat(result).isEqualTo("—");
    }


    //--------------------tests for getFormattedReleaseDate() ------------------
    @Test
    @DisplayName("getFormattedReleaseDate() - should return formatted release date")
    void getFormattedReleaseDate_ShouldReturnFormattedReleaseDate() {
        TmdbMovieDto movie = new TmdbMovieDto();
        movie.setReleaseDate("2026-05-26");

        String result = movie.getFormattedReleaseDate();

        assertThat(result).isEqualTo("Sortie le 26 mai 2026");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {" ", "invalide-date"})
    @DisplayName("getFormattedReleaseDate() - should return A venir when release date is null, blank or invalid")
    void getFormattedReleaseDate_ShouldReturnAVenirWhenReleaseDateIsNull(String ignoredInput) {
        TmdbMovieDto movie = new TmdbMovieDto();

        String result = movie.getFormattedReleaseDate();

        assertThat(result).isEqualTo("A venir");
    }

    //--------------------tests getFormattedRuntime()----------------------------

    @ParameterizedTest(name = "Test runtime= {0} -> {1}")
    @MethodSource("formattedRuntimeCases")
    void getFormattedRuntime_ShouldReturnFormattedRuntime(int runtime, String expected) {
        TmdbMovieDto movie = new TmdbMovieDto();
        movie.setRuntime(runtime);

        assertThat(movie.getFormattedRuntime()).isEqualTo(expected);
    }

    static Stream<Arguments> formattedRuntimeCases() {
        return Stream.of(
                Arguments.of(148, "2h 28min"),
                Arguments.of(120, "2h"),
                Arguments.of(45, "45min"),
                Arguments.of(0, "Durée inconnue")
        );
    }

    @Test
    @DisplayName("getFormattedRuntime() - should return unknown runtime when runtime is null")
    void getFormattedRuntime_ShouldReturnUnknownRuntimeWhenRuntimeIsNull() {
        TmdbMovieDto movie = new TmdbMovieDto();
        movie.setRuntime(null);

        String result = movie.getFormattedRuntime();

        assertThat(result).isEqualTo("Durée inconnue");
    }

}

