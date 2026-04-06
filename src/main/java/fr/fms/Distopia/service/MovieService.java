package fr.fms.Distopia.service;

import fr.fms.Distopia.dao.CinemaRepository;
import fr.fms.Distopia.dao.MovieRepository;

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

    //-------les films d'un cinéma (sauf ceux supprimés)-------------
    public List<Movie> getByCinema(Long cinemaId) {
        return movieRepository.findByCinemaIdAndDeletedFalse(cinemaId);
    }

    //-----tous les films (même supprimés)-----------------
    public List<Movie> getAll() {
        return movieRepository.findAll();
    }

}
