package fr.fms.Distopia.web;

import fr.fms.Distopia.tmdb.TmdbClient;
import fr.fms.Distopia.tmdb.dto.TmdbMovieDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IndexControllerTest {
    @Mock
    private Model model;
    @Mock
    private TmdbClient  tmdbClient;
    @InjectMocks
    private IndexController indexController;


    //------------------tests for index()---------------------------------
    @Test
    @DisplayName("index() - adds enriched tmdb movies to model and returns index view")
    void index_ShouldAddEnrichedTmdbMoviesToModelAndReturnIndexView() {
        TmdbMovieDto movie = new TmdbMovieDto();
        movie.setId(1L);
        movie.setTitle("Inception");

        TmdbMovieDto enrichedMovie = new TmdbMovieDto();
        enrichedMovie.setId(1L);
        enrichedMovie.setTitle("Inception");
        enrichedMovie.setRuntime(148);

        List<TmdbMovieDto> rawMovies = List.of(movie);
        List<TmdbMovieDto> enrichedMovies = List.of(enrichedMovie);

        when(tmdbClient.getNowPlaying()).thenReturn(rawMovies);
        when(tmdbClient.getUpcoming()).thenReturn(rawMovies);
        when(tmdbClient.enrichWithDetails(rawMovies)).thenReturn(enrichedMovies);

        String view = indexController.index(model);

        assertThat(view).isEqualTo("index");

        verify(model).addAttribute("imgBase", TmdbClient.IMG_BASE);
        verify(model).addAttribute("nowPlaying", enrichedMovies);
        verify(model).addAttribute("upcoming", enrichedMovies);

        verify(tmdbClient).getNowPlaying();
        verify(tmdbClient).getUpcoming();
        verify(tmdbClient,times(2)).enrichWithDetails(rawMovies);
    }

    @Test
    @DisplayName("index() - limits now playing and upcoming movies to 8 before enrichment")
    void index_ShouldLimitNowPlayingAndUpcomingMoviesToTenBeforeEnrichment() {
        List<TmdbMovieDto> movies = IntStream.rangeClosed(1, 15)
                .mapToObj(i -> {
                    TmdbMovieDto movie = new TmdbMovieDto();
                    movie.setId((long) i);
                    movie.setTitle("Movie " + i);
                    return movie;
                }).toList();

        when(tmdbClient.getNowPlaying()).thenReturn(movies);
        when(tmdbClient.getUpcoming()).thenReturn(movies);

        when(tmdbClient.enrichWithDetails(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String view = indexController.index(model);

        assertThat(view).isEqualTo("index");

        verify(tmdbClient, times(2)).enrichWithDetails(argThat(list ->
                list != null && list.size() == 8
        ));

        verify(model).addAttribute(eq("nowPlaying"), argThat(value ->
                value instanceof List<?> list && list.size() == 8
        ));

        verify(model).addAttribute(eq("upcoming"), argThat(value ->
                value instanceof List<?> list && list.size() == 8
        ));
    }

    @Test
    @DisplayName("index() - adds empty lists when tmdb client throws exception")
    void index_ShouldAddEmptyListsWhenTmdbClientThrowsException() {
        when(tmdbClient.getNowPlaying()).thenThrow(new RuntimeException("tmdb error"));

        String view = indexController.index(model);

        assertThat(view).isEqualTo("index");

        verify(model).addAttribute("imgBase", TmdbClient.IMG_BASE);
        verify(model).addAttribute("nowPlaying", List.of());
        verify(model).addAttribute("upcoming", List.of());

        verify(tmdbClient).getNowPlaying();
        verify(tmdbClient, never()).getUpcoming();
        verify(tmdbClient, never()).enrichWithDetails(anyList());
    }
}
