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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
 class SeanceServiceTest {
    @Mock
    private SeanceRepository seanceRepository;
    @Mock
    private MovieRepository movieRepository;
    @Mock
    private CinemaRepository cinemaRepository;
    @InjectMocks
    private SeanceService seanceService;

    private Seance seance;
    private Movie movie;
    private Cinema cinema;
    private final LocalDateTime futureDate = LocalDateTime.now().plusDays(7);

    @BeforeEach
    void setUp() {
        cinema = new Cinema();
        cinema.setId(1L);
        cinema.setName("Cinema Test");

        movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Movie Test");

        seance = new Seance();
        seance.setId(1L);
        seance.setDateTime(futureDate);
        seance.setAvailableSeats(100);
        seance.setPrice(12.0);
        seance.setMovie(movie);
        seance.setCinema(cinema);
        seance.setReservations(new ArrayList<>());
    }

    //------------------tests du save()-----------------------
    @Test
    @DisplayName("save() - creates a new seance when id is null")
    void save_shouldCreateNewSeanceWhenIdIsNull() {
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(cinemaRepository.findById(1L)).thenReturn(Optional.of(cinema));
        when(seanceRepository.save(any(Seance.class))).thenAnswer(i -> i.getArgument(0));

        Seance result = seanceService.save(null, futureDate, 100, 10.0,1L,1L);

        assertThat(result.getAvailableSeats()).isEqualTo(100);
        assertThat(result.getPrice()).isEqualTo(10.0);
        assertThat(result.getCinema()).isEqualTo(cinema);
        assertThat(result.getMovie()).isEqualTo(movie);
        verify(seanceRepository).save(any(Seance.class));
    }
}
