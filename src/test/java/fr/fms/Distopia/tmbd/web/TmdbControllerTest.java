package fr.fms.Distopia.tmbd.web;

import fr.fms.Distopia.service.MovieService;
import fr.fms.Distopia.tmdb.TmdbClient;
import fr.fms.Distopia.tmdb.dto.TmdbGenreDto;
import fr.fms.Distopia.tmdb.dto.TmdbMovieDto;
import fr.fms.Distopia.tmdb.web.TmbdController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.util.List;

@ExtendWith(MockitoExtension.class)
class TmdbControllerTest {

    @Mock
    private TmdbClient tmdbClient;
    @Mock
    private MovieService movieService;
    @Mock
    private Model model;
    @Mock
    private RedirectAttributes redirectAttributes;
    @InjectMocks
    private TmbdController tmbdController;

    private TmdbMovieDto validDetail;

    @BeforeEach
    void setUp() {
        TmdbGenreDto genre = new  TmdbGenreDto();
        genre.setName("Sci-Fi");

        validDetail = new TmdbMovieDto();
        validDetail.setId(1L);
        validDetail.setTitle("Inception");
        validDetail.setOverview("Description Inception");
        validDetail.setRuntime(148);
        validDetail.setPosterPath("/inception.jpg");
        validDetail.setGenres(List.of(genre));
    }
}
