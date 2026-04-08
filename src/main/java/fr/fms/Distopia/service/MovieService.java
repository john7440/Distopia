package fr.fms.Distopia.service;

import fr.fms.Distopia.dao.CinemaRepository;
import fr.fms.Distopia.dao.MovieRepository;

import fr.fms.Distopia.dao.SeanceRepository;
import fr.fms.Distopia.entities.Movie;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MovieService {
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private CinemaRepository cinemaRepository;
    @Autowired
    private SeanceRepository seanceRepository;

    //-------les films d'un cinéma (sauf ceux supprimés)-------------
    public List<Movie> getByCinema(Long cinemaId) {
        return movieRepository.findByCinemasIdAndDeletedFalse(cinemaId);
    }

    //----------find by id----------------------
    public Optional<Movie> findById(Long id) {
        return movieRepository.findById(id);
    }

    //-----tous les films (même supprimés)-----------------
    public List<Movie> getAll() {
        return movieRepository.findAll();
    }

    //--------------------créer ou modifier un film------------------
    @Transactional
    public Movie save(Long id, String title, String description,
                      int duration, String genre, String imageUrl,
                      List<Long> cinemaIds) {

        Movie movie = (id != null)
                ? movieRepository.findById(id).orElse(new Movie())
                : new Movie();

        movie.setTitle(title);
        movie.setDescription(description);
        movie.setDuration(duration);
        movie.setGenre(genre);
        movie.setImageUrl(imageUrl);

        movie.getCinemas().forEach(c -> c.getMovies().remove(movie));
        movie.getCinemas().clear();

        if (cinemaIds != null) {
            cinemaIds.forEach(cinemaId ->
                    cinemaRepository.findById(cinemaId).ifPresent(cinema -> {
                        cinema.getMovies().add(movie);
                        movie.getCinemas().add(cinema);
                    })
            );
        }
        return movieRepository.save(movie);
    }

    //----------------suppression d'un film (il reste en bdd) + désactivation des séances liées -----------
    public void softDelete(Long id) {
        movieRepository.findById(id).ifPresent(movie -> {
            movie.setDeleted(true);
            movie.getSeances().forEach(seance -> seance.setAvailableSeats(0));
            seanceRepository.saveAll(movie.getSeances());
            movieRepository.save(movie);
        });
    }

}
