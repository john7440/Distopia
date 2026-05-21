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
        Cinema cinema = new Cinema();
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
}
