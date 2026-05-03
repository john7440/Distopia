package fr.fms.Distopia.service;

import fr.fms.Distopia.dao.CinemaRepository;
import fr.fms.Distopia.dao.MovieRepository;
import fr.fms.Distopia.dao.SeanceRepository;
import fr.fms.Distopia.entities.Cinema;
import fr.fms.Distopia.entities.Movie;
import fr.fms.Distopia.entities.Seance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;
    @Mock
    private CinemaRepository cinemaRepository;
    @Mock
    private SeanceRepository seanceRepository;
    @InjectMocks
    private MovieService movieService;

    private Movie movie;
    private Cinema cinema;
    private Seance seance;

    @BeforeEach
    void setUp() {
        cinema = new Cinema();
        cinema.setId(1L);
        cinema.setName("Cinema Test");

        seance = new Seance();
        seance.setId(1L);
        seance.setAvailableSeats(100);

        movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Movie Test");
        movie.setDeleted(false);
        movie.setSeances(new ArrayList<>(List.of(seance)));
        movie.setCinemas(new ArrayList<>(List.of(cinema)));
    }

    //------------------tests du softDelete() ----------------------------------
    @Test
    @DisplayName("softDelete() -  marks movie as deleted and sets all seance seats at 0")
    void softDelete_shouldMarkMovieAsDeletedAndZeroOutSeats(){
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));

        movieService.softDelete(1L);

        assertThat(movie.isDeleted()).isTrue();
        assertThat(seance.getAvailableSeats()).isZero();
        verify(seanceRepository).saveAll(movie.getSeances());
        verify(movieRepository).save(movie);
    }

    @Test
    @DisplayName("softDelete() - does nothing when movie id not found")
    void softDelete_shouldDoNothingWhenMovieIdNotFound(){
        when(movieRepository.findById(99L)).thenReturn(Optional.empty());

        movieService.softDelete(99L);

        verify(movieRepository, never()).save(any());
        verify(seanceRepository,never()).saveAll(any());
    }

    //-------------------test du save()-------------------------------------------

    @Test
    @DisplayName("save() - creates a new movie when id is null")
    void save_shouldCreateNewMovie_whenIdIsNull() {
        when(cinemaRepository.findById(1L)).thenReturn(Optional.of(cinema));
        when(movieRepository.save(any(Movie.class))).thenAnswer(inv -> inv.getArgument(0));

        Movie result = movieService.save(null, "Interstellar", "A space movie",
                169, "Sci-Fi", "url.jpg", "trailer.mp4", List.of(1L));

        assertThat(result.getTitle()).isEqualTo("Interstellar");
        assertThat(result.getCinemas()).contains(cinema);
        verify(movieRepository).save(any(Movie.class));
    }

    @Test
    @DisplayName("save() - updates existing movie and resets cinema associations")
    void save_shouldUpdateExistingMovieAndResetCinemas_whenIdExists() {
        Cinema newCinema = new Cinema();
        newCinema.setId(2L);

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(cinemaRepository.findById(2L)).thenReturn(Optional.of(newCinema));
        when(movieRepository.save(any(Movie.class))).thenAnswer(inv -> inv.getArgument(0));

        Movie result = movieService.save(1L, "Inception V2", "Updated", 148, "Thriller",
                "new.jpg", "new_trailer.mp4", List.of(2L));

        assertThat(result.getTitle()).isEqualTo("Inception V2");
        assertThat(result.getCinemas()).containsOnly(newCinema);
        assertThat(result.getCinemas()).doesNotContain(cinema);
    }

    //--------------------test de getByCinema()----------------------
    @Test
    @DisplayName("getByCinema() - returns only non-deleted movies for a cinema")
    void findActiveByCinema_shouldReturnOnlyNonDeletedMovies() {
        Movie deletedMovie = new Movie();
        deletedMovie.setDeleted(true);

        when(movieRepository.findByCinemasIdAndDeletedFalse(1L))
                .thenReturn(List.of(movie));

        List<Movie> result = movieService.getByCinema(1L);

        assertThat(result).containsOnly(movie);
        assertThat(result).doesNotContain(deletedMovie);
    }

}
