package fr.fms.Distopia.service;

import fr.fms.Distopia.entities.Cinema;
import fr.fms.Distopia.entities.Movie;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class SeanceGeneratorServiceTest {
    @Mock
    private TmdbClient tmdbClient;
    @Mock
    private MovieService movieService;
    @Mock
    SeanceService seanceService;
    @Mock
    private CinemaService cinemaService;
    @InjectMocks
    private SeanceGeneratorService seanceGeneratorService;

    private Cinema cinema;
    private Movie movie;
    private TmdbMovieDto tmdbMovieDto;
    private TmdbMovieDto tmdbDetail;

    private static final LocalDateTime START_DATE = LocalDateTime.now().plusDays(1).withHour(0)
            .withMinute(0).withSecond(0).withNano(0);

    @BeforeEach
    void setUp() {
        cinema = new Cinema();
        cinema.setId(1L);
        cinema.setName("Cinema Test");

        movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Inception");

        TmdbGenreDto genre =  new TmdbGenreDto();
        genre.setName("Sci-Fi");

        tmdbMovieDto = new TmdbMovieDto();
        tmdbMovieDto.setId(1L);
        tmdbMovieDto.setTitle("Inception");

        tmdbDetail = new TmdbMovieDto();
        tmdbDetail.setId(1L);
        tmdbDetail.setTitle("Inception");
        tmdbDetail.setOverview("Description Inception");
        tmdbDetail.setRuntime(148);
        tmdbDetail.setPosterPath("/inception.jpg");
        tmdbDetail.setGenres(List.of(genre));
    }

    //---------------tests for importAndGenerate()----------------------
    @Test
    @DisplayName("importAndGenerate() - returns error when no cinemas in database")
    void importAndGenerate_ShouldReturnsErrorWhenNoCinemasInDatabase() {
        when(cinemaService.getAll()).thenReturn(List.of());

        SeanceGeneratorService.GeneratorResult result = seanceGeneratorService.importAndGenerate();

        assertThat(result.errors()).isNotEmpty();
        assertThat(result.moviesImported()).isZero();
        assertThat(result.seancesCreated()).isZero();
    }

    @Test
    @DisplayName("importAndGenerate() - does not call TMBD when no cinemas in database")
    void importAndGenerate_ShouldNotCallTmdbWhenNoCinemas() {
        when(cinemaService.getAll()).thenReturn(List.of());

        seanceGeneratorService.importAndGenerate();

        verify(tmdbClient, never()).getNowPlaying();
    }

    @Test
    @DisplayName("importAndGenerate() - adds errors when TMDB returns no movies")
    void importAndGenerate_ShouldAddErrorsWhenNoMovies() {
        when(cinemaService.getAll()).thenReturn(List.of(cinema));
        when(tmdbClient.getNowPlaying()).thenReturn(List.of());

        SeanceGeneratorService.GeneratorResult result = seanceGeneratorService.importAndGenerate();

        assertThat(result.errors()).isNotEmpty();
        assertThat(result.moviesImported()).isZero();
    }

    @Test
    @DisplayName("importAndGenerate() - imports new movies from TMDB")
    void importAndGenerate_ShouldImportNewMoviesFromTmdb() {
    when(cinemaService.getAll()).thenReturn(List.of(cinema));
    when(tmdbClient.getNowPlaying()).thenReturn(List.of(tmdbMovieDto));
    when(tmdbClient.getDetail(1L)).thenReturn(tmdbDetail);
    when(tmdbClient.getTrailerUrl(1L)).thenReturn("https://youtube.com/emded/test");
    when(movieService.getAllActive()).thenReturn(List.of());
    when(movieService.save(any(), any(),any(), any(), anyInt(), any(), any(), any(), any(), any()))
                .thenReturn(movie);

    SeanceGeneratorService.GeneratorResult result = seanceGeneratorService.importAndGenerate();

    assertThat(result.moviesImported()).isEqualTo(1);
    verify(movieService, atLeastOnce()).save(isNull(), any(),eq("Inception"), anyString(), eq(148),
            eq("Sci-Fi"), contains("/inception.jpg"), anyString(), any(), any());
    }

    @Test
    @DisplayName("importAndGenerate() - skips movie already existing in database")
    void importAndGenerate_ShouldSkipMovieAlreadyInDatabase() {
        when(cinemaService.getAll()).thenReturn(List.of(cinema));
        when(tmdbClient.getNowPlaying()).thenReturn(List.of(tmdbMovieDto));
        when(tmdbClient.getDetail(1L)).thenReturn(tmdbDetail);
        when(movieService.getAllActive()).thenReturn(List.of(movie));

        SeanceGeneratorService.GeneratorResult result = seanceGeneratorService.importAndGenerate();

        assertThat(result.moviesImported()).isZero();
        verify(movieService, never()).save(isNull(), any(),anyString(), any(), anyInt(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("importAndGenerate() - adds error to list when detail fetch fails for a movie")
    void importAndGenerate_ShouldAddErrorToListWhenDetailFetchFails() {
        when(cinemaService.getAll()).thenReturn(List.of(cinema));
        when(tmdbClient.getNowPlaying()).thenReturn(List.of(tmdbMovieDto));
        when(tmdbClient.getDetail(1L)).thenThrow(new RuntimeException("API timeout"));
        when(movieService.getAllActive()).thenReturn(List.of());

        SeanceGeneratorService.GeneratorResult result = seanceGeneratorService.importAndGenerate();

        assertThat(result.errors()).anyMatch(e -> e.contains("1"));
    }

    @Test
    @DisplayName("importAndGenerate() - returns no errors on full successful run")
    void importAndGenerate_ShouldReturnNoErrorsOnSuccess() {
        when(cinemaService.getAll()).thenReturn(List.of(cinema));
        when(tmdbClient.getNowPlaying()).thenReturn(List.of(tmdbMovieDto));
        when(tmdbClient.getDetail(1L)).thenReturn(tmdbDetail);
        when(movieService.getAllActive()).thenReturn(List.of(movie));
        when(movieService.save(any(), any(),any(), any(), anyInt(), any(),any(), any(), any(), any()))
                .thenReturn(movie);
        when(seanceService.getByMovieAndCinema(1L, 1L)).thenReturn(List.of());

        SeanceGeneratorService.GeneratorResult result = seanceGeneratorService.importAndGenerate();

        assertThat(result.errors()).isEmpty();
        assertThat(result.hasErrors()).isFalse();
    }
}
