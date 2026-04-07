package fr.fms.Distopia.service;

import fr.fms.Distopia.dao.MovieRepository;
import fr.fms.Distopia.dao.SeanceRepository;
import fr.fms.Distopia.entities.Seance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeanceService {
    @Autowired
    private SeanceRepository seanceRepository;
    @Autowired
    private MovieRepository movieRepository;

    public List<Seance> getByMovie(Long movieId){
        return seanceRepository.findByMovieIdOrderByDateTimeAsc(movieId);
    }
}
