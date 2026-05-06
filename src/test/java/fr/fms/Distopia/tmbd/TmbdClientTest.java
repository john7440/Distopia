package fr.fms.Distopia.tmbd;

import fr.fms.Distopia.tmdb.TmdbClient;
import fr.fms.Distopia.tmdb.dto.TmdbGenreDto;
import fr.fms.Distopia.tmdb.dto.TmdbMovieDto;
import fr.fms.Distopia.tmdb.dto.TmdbSearchResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TmbdClientTest {
    @Mock
    private RestTemplate restTemplate;
    @InjectMocks
    private TmdbClient tmdbClient;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tmdbClient, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(tmdbClient, "apiKey", "FAKE_KEY_TEST");
    }

    //-----------------tests for search()-----------------
    @Test
    @DisplayName("search() - returns a list of movies when TMDB responds")
    void search_ShouldReturnMoviesWhenTmdbResponds() {
        TmdbMovieDto movie =  new TmdbMovieDto();
        movie.setId(26L);
        movie.setTitle("Inception");

        TmdbSearchResponse response = new TmdbSearchResponse();
        response.setResults(List.of(movie));

        when(restTemplate.getForObject(contains("search/movie"), eq(TmdbSearchResponse.class))).thenReturn(response);

        List<TmdbMovieDto> results = tmdbClient.search("Inception");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Inception");
    }

    @Test
    @DisplayName("search() - return an empty list when tmdb responds null")
    void search_ShouldReturnEmptyListWhenTmdbRespondsNull() {
        when(restTemplate.getForObject(contains("search/movie"), eq(TmdbSearchResponse.class))).thenReturn(null);
        List<TmdbMovieDto> results = tmdbClient.search("film inexistant");

        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("search() - url contains the correct language(fr-FR)")
    void search_ShouldCallApiWithFrenchLanguage() {
        TmdbSearchResponse emptyResponse = new TmdbSearchResponse();
        emptyResponse.setResults(List.of());
        when(restTemplate.getForObject(anyString(), eq(TmdbSearchResponse.class)))
                .thenReturn(emptyResponse);

        tmdbClient.search("Inception");

        verify(restTemplate).getForObject(argThat((String url) -> url.contains("language=fr-FR")), eq(TmdbSearchResponse.class));
    }

    //------------------------------------tests for getDetail()-------------------------

    @Test
    @DisplayName("getDetail() - returns the movie with runtime and genres")
    void getDetail_ShouldReturnMovieWithRuntimeAndGenres() {
        TmdbGenreDto genre =  new TmdbGenreDto();
        genre.setName("Sci-Fi");

        TmdbMovieDto movie = new TmdbMovieDto();
        movie.setId(26L);
        movie.setTitle("Inception");
        movie.setRuntime(148);
        movie.setGenres(List.of(genre));

        when(restTemplate.getForObject(contains("/movie/26"), eq(TmdbMovieDto.class))).thenReturn(movie);
        TmdbMovieDto result = tmdbClient.getDetail(26L);

        assertThat(result).isNotNull();
        assertThat(result.getRuntime()).isEqualTo(148);
        assertThat(result.getGenres()).hasSize(1);
        assertThat(result.getGenres().get(0).getName()).isEqualTo("Sci-Fi");
    }
}
