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

import static org.assertj.core.api.Assertions.assertThat;
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
}
