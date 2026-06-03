package fr.fms.Distopia.tmdb;

import fr.fms.Distopia.tmdb.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TmdbClientTest {
    @Mock
    private RestTemplate restTemplate;
    @InjectMocks
    private TmdbClient tmdbClient;

    private TmdbMovieDto movie;

    @BeforeEach
    void setUp() {
        tmdbClient = new TmdbClient();
        restTemplate = mock(RestTemplate.class);

        movie = new TmdbMovieDto();
        movie.setId(1L);
        movie.setTitle("Inception");
        movie.setReleaseDate(LocalDate.now().toString());

        ReflectionTestUtils.setField(tmdbClient, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(tmdbClient, "apiKey", "FAKE_KEY_TEST");
    }

    //-----------------tests for search()-----------------
    @Test
    @DisplayName("search() - returns a list of movies when TMDB responds")
    void search_ShouldReturnMoviesWhenTmdbResponds() {
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

    @Test
    @DisplayName("getDetail() - returns null if the movie is not found on tmdb")
    void getDetail_ShouldReturnsNullIfMovieWithNotFound() {
        when(restTemplate.getForObject(contains("/movie/999"), eq(TmdbMovieDto.class))).thenReturn(null);

        assertThat(tmdbClient.getDetail(999L)).isNull();
    }

    //-----------------------tests for getTrailerUrl()----------------
    @Test
    @DisplayName("getTrailerUrl() - returns embed Youtube trailer url")
    void getTrailerUrl_ShouldReturnEmbedTrailerUrl() {
        TmdbVideoDto trailer =  new TmdbVideoDto();
        trailer.setKey("s5CkFFnFuWs");
        trailer.setSite("YouTube");
        trailer.setType("Trailer");

        TmdbVideosResponse response =  new TmdbVideosResponse();
        response.setResults(List.of(trailer));

        when(restTemplate.getForObject(contains("/videos"), eq(TmdbVideosResponse.class))).thenReturn(response);

        String url =  tmdbClient.getTrailerUrl(27205L);

        assertThat(url).isEqualTo("https://www.youtube.com/embed/s5CkFFnFuWs");
    }

    @Test
    @DisplayName("getTrailerUrl() - returns null if no trailer found")
    void getTrailerUrl_ShouldReturnNullIfNoTrailerFound() {
        TmdbVideoDto teaser =  new TmdbVideoDto();
        teaser.setKey("test123");
        teaser.setSite("Vimeo");
        teaser.setType("Teaser");

        TmdbVideosResponse response =  new TmdbVideosResponse();
        response.setResults(List.of(teaser));

        when(restTemplate.getForObject(contains("/videos"), eq(TmdbVideosResponse.class))).thenReturn(response);

        assertThat(tmdbClient.getTrailerUrl(27205L)).isNull();
    }

    @Test
    @DisplayName("getTrailerUrl() - returns null if tmdb returns null")
    void getTrailerUrl_shouldReturnNullIfTmdbReturnsNull() {
        when(restTemplate.getForObject(contains("/videos"), eq(TmdbVideosResponse.class)))
                .thenReturn(null);

        assertThat(tmdbClient.getTrailerUrl(27205L)).isNull();
    }

    @Test
    @DisplayName("getTrailerUrl() - ignore Vimeo videos and accept only YouTube's one")
    void getTrailerUrl_ShouldIgnoreOtherVideoThanYoutube() {
        TmdbVideoDto vimeo =  new TmdbVideoDto();
        vimeo.setKey("vimeo123");
        vimeo.setSite("Vimeo");
        vimeo.setType("Trailer");

        TmdbVideoDto youtube =  new TmdbVideoDto();
        youtube.setKey("youtube123");
        youtube.setSite("YouTube");
        youtube.setType("Trailer");

        TmdbVideosResponse response =  new TmdbVideosResponse();
        response.setResults(List.of(vimeo,youtube));

        when(restTemplate.getForObject(contains("/videos"), eq(TmdbVideosResponse.class)))
            .thenReturn(response);

        assertThat(tmdbClient.getTrailerUrl(1L)).isEqualTo("https://www.youtube.com/embed/youtube123");
    }

    //------------------------tests for getNowPlaying()------------------------
    @Test
    @DisplayName("getNowPlaying() - returns movies when response exists")
    void getNowPlaying_ShouldReturnMoviesWhenResponseExists() {
        TmdbSearchResponse response =  new TmdbSearchResponse();
        response.setResults(List.of(movie));

        when(restTemplate.getForObject(anyString(), eq(TmdbSearchResponse.class)))
            .thenReturn(response);

        List<TmdbMovieDto> result = tmdbClient.getNowPlaying();

        assertThat(result).containsExactly(movie);
    }

    @Test
    @DisplayName("getNowPlaying() - returns empty list when response is null")
    void getNowPlaying_ShouldReturnEmptyListWhenResponseIsNull() {
        when(restTemplate.getForObject(anyString(), eq(TmdbSearchResponse.class))).thenReturn(null);

        List<TmdbMovieDto> result = tmdbClient.getNowPlaying();

        assertThat(result).isEmpty();
    }

    //------------------tests for getUpcoming() ---------------------------

    @Test
    @DisplayName("getUpcoming() - returns upcoming movies when response exists")
    void getUpcoming_ShouldReturnUpcomingMoviesWhenResponseExists() {
        TmdbSearchResponse response =  new TmdbSearchResponse();
        response.setResults(List.of(movie));

        when(restTemplate.getForObject(anyString(), eq(TmdbSearchResponse.class))).thenReturn(response);

        List<TmdbMovieDto> result = tmdbClient.getUpcoming();

        assertThat(result).containsExactly(movie);
    }

    @Test
    @DisplayName("getUpcoming() - returns empty list when response is null")
    void getUpcoming_ShouldReturnEmptyListWhenResponseIsNull() {
        when(restTemplate.getForObject(anyString(), eq(TmdbSearchResponse.class)))
                .thenReturn(null);

        List<TmdbMovieDto> result = tmdbClient.getUpcoming();

        assertThat(result).isEmpty();
    }

    //-----------------------tests for enrichWithDetails()------------------
    @Test
    @DisplayName("enrichWithDetails() - should return empty list when movies are null")
    void enrichWithDetails_ShouldReturnEmptyListWhenMoviesAreNull() {
        List<TmdbMovieDto> result = tmdbClient.enrichWithDetails(null);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("enrichWithDetails() - should return empty list when movies are empty")
    void enrichWithDetails_ShouldReturnEmptyListWhenMoviesAreEmpty() {
        List<TmdbMovieDto> result = tmdbClient.enrichWithDetails(List.of());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("enrichWithDetails() - should enrich movie with runtime and genres")
    void enrichWithDetails_ShouldEnrichMovieWithRuntimeAndGenres() {
        movie.setId(1L);
        movie.setTitle("Inception");

        TmdbGenreDto genre = new TmdbGenreDto();
        genre.setName("Science-Fiction");

        TmdbMovieDto detail = new TmdbMovieDto();
        detail.setRuntime(148);
        detail.setGenres(List.of(genre));

        when(tmdbClient.getDetail(1L)).thenReturn(detail);

        List<TmdbMovieDto> result = tmdbClient.enrichWithDetails(List.of(movie));

        assertThat(result).hasSize(1);

        TmdbMovieDto enrichedMovie = result.get(0);

        assertThat(enrichedMovie.getRuntime()).isEqualTo(148);
        assertThat(enrichedMovie.getGenres())
                .extracting(TmdbGenreDto::getName)
                .containsExactly("Science-Fiction");
    }

    @Test
    @DisplayName("enrichWithDetails() - should enrich overview when missing")
    void enrichWithDetails_ShouldEnrichOverviewWhenMissing() {
        movie.setId(1L);
        movie.setOverview(null);

        TmdbMovieDto detail = new TmdbMovieDto();
        detail.setOverview("test overview");

        when(tmdbClient.getDetail(1L)).thenReturn(detail);

        List<TmdbMovieDto> result = tmdbClient.enrichWithDetails(List.of(movie));

        assertThat(result.get(0).getOverview()).isEqualTo("test overview");
    }

    @Test
    @DisplayName("enrichWithDetails() - should not overwrite existing overview")
    void enrichWithDetails_ShouldNotOverwriteExistingOverview() {
        movie.setId(1L);
        movie.setOverview("Existing overview");

        TmdbMovieDto detail = new TmdbMovieDto();
        detail.setOverview("Detailed overview");

        when(tmdbClient.getDetail(1L)).thenReturn(detail);

        List<TmdbMovieDto> result = tmdbClient.enrichWithDetails(List.of(movie));

        assertThat(result.get(0).getOverview()).isEqualTo("Existing overview");
    }

    @Test
    @DisplayName("enrichWithDetails() - should enrich poster path when missing")
    void enrichWithDetails_ShouldEnrichPosterPathWhenMissing() {
        TmdbMovieDto movie = new TmdbMovieDto();
        movie.setId(1L);
        movie.setPosterPath(null);

        TmdbMovieDto detail = new TmdbMovieDto();
        detail.setPosterPath("/poster.jpg");

        when(tmdbClient.getDetail(1L)).thenReturn(detail);

        List<TmdbMovieDto> result = tmdbClient.enrichWithDetails(List.of(movie));

        assertThat(result.get(0).getPosterPath()).isEqualTo("/poster.jpg");
    }

    @Test
    @DisplayName("enrichWithDetails() - should not overwrite existing poster path")
    void enrichWithDetails_ShouldNotOverwriteExistingPosterPath() {
        TmdbMovieDto movie = new TmdbMovieDto();
        movie.setId(1L);
        movie.setPosterPath("/existing.jpg");

        TmdbMovieDto detail = new TmdbMovieDto();
        detail.setPosterPath("/detail.jpg");

        when(tmdbClient.getDetail(1L)).thenReturn(detail);

        List<TmdbMovieDto> result = tmdbClient.enrichWithDetails(List.of(movie));

        assertThat(result.get(0).getPosterPath()).isEqualTo("/existing.jpg");
    }
}
