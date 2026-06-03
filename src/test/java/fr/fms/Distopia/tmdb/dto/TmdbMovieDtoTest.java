package fr.fms.Distopia.tmdb.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;


import java.util.List;
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

    //--------------------tests for  getDisplayGenres()----------------------------
    @Test
    @DisplayName("getDisplayGenres() - should return up to three genre names")
    void getDisplayGenres_ShouldReturnUpToThreeGenreNames() {
        TmdbGenreDto genre1 = new TmdbGenreDto();
        genre1.setName("Action");

        TmdbGenreDto genre2 = new TmdbGenreDto();
        genre2.setName("Science-Fiction");

        TmdbGenreDto genre3 = new TmdbGenreDto();
        genre3.setName("Adventure");

        TmdbGenreDto genre4 = new TmdbGenreDto();
        genre4.setName("Fantasy");

        TmdbMovieDto movie = new TmdbMovieDto();
        movie.setGenres(List.of(genre1, genre2, genre3, genre4));

        List<String> result = movie.getDisplayGenres();

        assertThat(result).containsExactly("Action", "Science-Fiction", "Adventure");
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @DisplayName("getDisplayGenres() - should return empty list when genres a null or empty")
    void getDisplayGenres_ShouldReturnEmptyListWhenGenresANullOrEmpty(String ignoredInput) {
        TmdbMovieDto movie = new TmdbMovieDto();

        assertThat(movie.getDisplayGenres()).isEmpty();
    }

    @Test
    @DisplayName("getDisplayGenres() - should ignore blank genre names")
    void getDisplayGenres_ShouldIgnoreBlankGenreNames() {
        TmdbGenreDto genre1 = new TmdbGenreDto();
        genre1.setName("Action");

        TmdbGenreDto genre2 = new TmdbGenreDto();
        genre2.setName("");

        TmdbGenreDto genre3 = new TmdbGenreDto();
        genre3.setName("Adventure");

        TmdbMovieDto movie = new TmdbMovieDto();
        movie.setGenres(List.of(genre1, genre2, genre3));

        List<String> result = movie.getDisplayGenres();

        assertThat(result).containsExactly("Action", "Adventure");
    }

    @Test
    @DisplayName("getDisplayGenres() - should ignore null genre names")
    void getDisplayGenres_ShouldIgnoreNullGenreNames() {
        TmdbGenreDto genre1 = new TmdbGenreDto();
        genre1.setName("Action");

        TmdbGenreDto genre2 = new TmdbGenreDto();
        genre2.setName(null);

        TmdbGenreDto genre3 = new TmdbGenreDto();
        genre3.setName("Adventure");

        TmdbMovieDto movie = new TmdbMovieDto();
        movie.setGenres(List.of(genre1, genre2, genre3));

        List<String> result = movie.getDisplayGenres();

        assertThat(result).containsExactly("Action", "Adventure");
    }
}

