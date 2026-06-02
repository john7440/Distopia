package fr.fms.Distopia.tmdb.web;

import fr.fms.Distopia.entities.Movie;
import fr.fms.Distopia.service.MovieService;
import fr.fms.Distopia.service.SeanceGeneratorService;
import fr.fms.Distopia.tmdb.TmdbClient;
import fr.fms.Distopia.tmdb.dto.TmdbGenreDto;
import fr.fms.Distopia.tmdb.dto.TmdbMovieDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
    @Mock
    private SeanceGeneratorService seanceGeneratorService;
    @InjectMocks
    private TmdbController tmdbController;

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

    //-------------------------------tests for importPage()--------------
    @Test
    @DisplayName("importPage() - returns admin-import-movies view")
    void importPage_ShouldReturnAdminImportMovieView() {
        String view = tmdbController.importPage(null,model);

        assertThat(view).isEqualTo("admin-import-movies");
    }

    @Test
    @DisplayName("importPage() - does not call tmdb if the query is null")
    void importPage_ShouldNotCallTmdb_WhenQueryIsNull() {
        tmdbController.importPage(null,model);

        verify(tmdbClient, never()).search(any());
    }

    @Test
    @DisplayName("importPage() - calls tmdb and add result to model if there is a query")
    void importPage_ShouldCallTmdbAndAddResultToModel() {
        TmdbMovieDto movie =  new TmdbMovieDto();
        movie.setTitle("Inception");
        when(tmdbClient.search("Inception")).thenReturn(List.of(movie));

        tmdbController.importPage("Inception",model);

        verify(tmdbClient).search("Inception");
        verify(model).addAttribute("results",List.of(movie));
        verify(model).addAttribute("query","Inception");
    }

    @Test
    @DisplayName("importPage() - always adds imgBase to the model")
    void importPage_ShouldAlwaysAddImgBaseToModel() {
        tmdbController.importPage(null,model);

        verify(model).addAttribute("imgBase",TmdbClient.IMG_BASE);
    }

    //-------------------------test for importMovie()--------------------------------
    @Test
    @DisplayName("importMovie() - imports movie and redirect with success message")
    void importMovie_ShouldImportMovieAndRedirectWithSuccessMessage() {
        when(tmdbClient.getDetail(1L)).thenReturn(validDetail);
        when(tmdbClient.getTrailerUrl(1L)).thenReturn("https://www.youtube.com/embed/test");
        when(movieService.save(any(),any(),any(),any(),anyInt(),any(),any(),any(), any(), any())).thenReturn(new Movie());
        when(seanceGeneratorService.generateForMovie(any()))
                .thenReturn(new SeanceGeneratorService.GeneratorResult(1, 3, List.of()));

        String view = tmdbController.importMovie(1L, null, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/admin/import-movies");
        verify(redirectAttributes).addFlashAttribute(eq("message"), contains("Inception"));
    }

    @Test
    @DisplayName("importMovie() - adds flash error when tmdb returns null")
    void importMovie_ShouldAddFlashErrorWhenTmdbReturnsNull() {
        when(tmdbClient.getDetail(99L)).thenReturn(null);

        String view = tmdbController.importMovie(99L, null, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/admin/import-movies");
        verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
        verify(movieService, never()).save(any(), any(), any(), any(), anyInt(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("importMovie() - use 'Inconnu' when genres are null")
    void importMovie_ShouldUseInconnuWhenGenresAreNull() {
        validDetail.setGenres(null);
        when(tmdbClient.getDetail(1L)).thenReturn(validDetail);
        when(tmdbClient.getTrailerUrl(1L)).thenReturn(null);
        when(movieService.save(any(), any(), any(), any(), anyInt(), any(), any(), any(), any(), any()))
                .thenReturn(new Movie());
        when(seanceGeneratorService.generateForMovie(any()))
                .thenReturn(new SeanceGeneratorService.GeneratorResult(1, 3, List.of()));

        tmdbController.importMovie(1L,null,redirectAttributes);

        verify(movieService).save(isNull(),any(), anyString(),anyString(),anyInt(),eq("Inconnu"),any(), any(), isNull(), any());
    }

    @Test
    @DisplayName("importMovie() - should use zero duration when runtime is null")
    void importMovie_ShouldUseZeroDurationWhenRuntimeIsNull() {
        validDetail.setRuntime(null);
        when(tmdbClient.getDetail(1L)).thenReturn(validDetail);
        when(tmdbClient.getTrailerUrl(1L)).thenReturn(null);
        when(movieService.save(any(), any(), any(), any(), anyInt(), any(), any(), any(), any(), any())).thenReturn(new Movie());
        when(seanceGeneratorService.generateForMovie(any()))
                .thenReturn(new SeanceGeneratorService.GeneratorResult(1, 3, List.of()));

        tmdbController.importMovie(1L,null,redirectAttributes);

        verify(movieService).save(isNull(),
                any(), anyString(), anyString(), eq(0), any(), any(), any(), isNull(), any());
    }

    @Test
    @DisplayName("importMovie() - keep query in redirection when query provided")
    void importMovie_ShouldKeepQueryInRedirectionWhenQueryProvided() {
        when(tmdbClient.getDetail(1L)).thenReturn(validDetail);
        when(tmdbClient.getTrailerUrl(1L)).thenReturn(null);
        when(movieService.save(any(), any(), any(), any(), anyInt(), any(), any(), any(), any(), any())).thenReturn(new Movie());
        when(seanceGeneratorService.generateForMovie(any()))
                .thenReturn(new SeanceGeneratorService.GeneratorResult(1, 3, List.of()));

        String view = tmdbController.importMovie(1L,"Inception",redirectAttributes);

        assertThat(view).isEqualTo("redirect:/admin/import-movies?query=Inception");
    }

    //------------------   tests for generateNowPlaying() ---------------------------

    @Test
    @DisplayName("generateNowPlaying() - adds success message and redirects to admin seances")
    void generateNowPlaying_ShouldAddSuccessMessageAndRedirectToAdminSeances() {
        SeanceGeneratorService.GeneratorResult result = new SeanceGeneratorService.GeneratorResult(
                2, 42, List.of());

        when(seanceGeneratorService.importAndGenerate()).thenReturn(result);

        String view = tmdbController.generateNowPlaying(redirectAttributes);

        assertThat(view).isEqualTo("redirect:/admin/seances");
        verify(redirectAttributes).addFlashAttribute("message",
                "2 films importés et 42 séances générées sur 7 jours");
        verify(redirectAttributes, never()).addFlashAttribute(eq("warning"), any());
    }

    @Test
    @DisplayName("generateNowPlaying() - adds warning message when generation has errors")
    void generateNowPlaying_ShouldAddWarningMessageWhenGenerationHasErrors() {
        SeanceGeneratorService.GeneratorResult result = new SeanceGeneratorService.GeneratorResult(
                1, 21, List.of("Erreur cinéma", "Erreur séance")
                );
        when(seanceGeneratorService.importAndGenerate()).thenReturn(result);

        String view = tmdbController.generateNowPlaying(redirectAttributes);

        assertThat(view).isEqualTo("redirect:/admin/seances");
        verify(redirectAttributes).addFlashAttribute("message",
                "1 films importés et 21 séances générées sur 7 jours");
        verify(redirectAttributes).addFlashAttribute("warning",
                        "Avertissements: Erreur cinéma | Erreur séance");
    }
}
