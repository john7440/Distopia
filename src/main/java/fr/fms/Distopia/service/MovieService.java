package fr.fms.Distopia.service;

import fr.fms.Distopia.dao.CinemaRepository;
import fr.fms.Distopia.dao.MovieRepository;

import fr.fms.Distopia.dao.SeanceRepository;
import fr.fms.Distopia.entities.Movie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    //-----tous les films (même supprimés)-----------------
    public List<Movie> getAll() {
        return movieRepository.findAll();
    }

    //--------------------créer ou modifier un film------------------
    public Movie save(Long id, String title, String description, int duration, String genre, Long cinemaId) {
        Movie movie = (id !=null) ? movieRepository.findById(id).orElse(new Movie()) : new Movie();
        movie.setTitle(title);
        movie.setDescription(description);
        movie.setDuration(duration);
        movie.setGenre(genre);
        if (cinemaId != null) {
            cinemaRepository.findById(cinemaId).ifPresent( cinema -> {
                if (!movie.getCinemas().contains(cinema)) {
                    movie.getCinemas().add(cinema);
                }
            });
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
