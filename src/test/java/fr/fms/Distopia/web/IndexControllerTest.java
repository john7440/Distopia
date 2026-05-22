package fr.fms.Distopia.web;


import fr.fms.Distopia.service.TownService;
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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndexControllerTest {
    @Mock
    private TownService townService;
    @Mock
    private Model model;
    @Mock
    private TmdbClient  tmdbClient;
    @InjectMocks
    private IndexController indexController;


    //------------------tests for index()---------------------------------
    @Test
    @DisplayName("index() - adds tmdb movies to model and returns index view")
    void index_ShouldAddTmdbMoviesToModelAndReturnIndexView() {
        TmdbMovieDto movie = new TmdbMovieDto();
        movie.setId(1L);
        movie.setTitle("Inception");

        when(tmdbClient.getNowPlaying()).thenReturn(List.of(movie));
        when(tmdbClient.getThisWeek()).thenReturn(List.of(movie));
        when(tmdbClient.getUpcoming()).thenReturn(List.of(movie));

        String view = indexController.index(model);

        assertThat(view).isEqualTo("index");

        verify(model).addAttribute("imgBase", TmdbClient.IMG_BASE);
        verify(model).addAttribute("nowPlaying", List.of(movie));
        verify(model).addAttribute("thisWeek",List.of(movie));
        verify(model).addAttribute("upcoming", List.of(movie));
    }

    @Test
    @DisplayName("index() - limits now playing and upcoming movies to 10")
    void index_ShouldLimitNowPlayingAndUpcomingMoviesToTen() {
        List<TmdbMovieDto> movies = IntStream.rangeClosed(1, 15)
                .mapToObj(i -> {
                    TmdbMovieDto movie = new TmdbMovieDto();
                    movie.setId((long) i);
                    movie.setTitle("Movie " + i);
                    return movie;
                }).toList();

        when(tmdbClient.getNowPlaying()).thenReturn(movies);
        when(tmdbClient.getThisWeek()).thenReturn(movies);
        when(tmdbClient.getUpcoming()).thenReturn(movies);

        indexController.index(model);

        verify(model).addAttribute(eq("nowPlaying"), argThat(list ->
                list instanceof List<?> l && l.size() == 10
        ));

        verify(model).addAttribute("thisWeek", movies);

        verify(model).addAttribute(eq("upcoming"), argThat(list ->
                list instanceof List<?> l && l.size() == 10
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
        verify(model).addAttribute("thisWeek", List.of());
        verify(model).addAttribute("upcoming", List.of());
    }
}
