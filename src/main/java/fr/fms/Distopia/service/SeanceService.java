package fr.fms.Distopia.service;

import fr.fms.Distopia.dao.MovieRepository;
import fr.fms.Distopia.dao.SeanceRepository;
import fr.fms.Distopia.entities.Movie;
import fr.fms.Distopia.entities.Seance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SeanceService {
    @Autowired
    private SeanceRepository seanceRepository;
    @Autowired
    private MovieRepository movieRepository;

    //---------------les séances d'un film---------------------
    public List<Seance> getByMovie(Long movieId){
        return seanceRepository.findByMovieIdOrderByDateTimeAsc(movieId);
    }

    //------------toutes les séances-------------
    public List<Seance> getAll(){
        return seanceRepository.findAll();
    }

    //-------------------créer ou modifier une séance-----------------------
    public Seance save(Long id, LocalDateTime dateTime, int availableSeats, double price, Long movieId) {
        Seance seance = (id!=null) ? seanceRepository.findById(id).orElse(new Seance()) : new Seance();

        seance.setDateTime(dateTime);
        seance.setAvailableSeats(availableSeats);
        seance.setPrice(price);

        if(movieId !=null){
            Movie movie = movieRepository.findById(movieId).orElseThrow();
            seance.setMovie(movie);
        }
        return seanceRepository.save(seance);
    }

    //-----------supprimer une séance (ajout vérif de réservation)-------------
    public void delete(Long id){
        Seance seance = seanceRepository.findById(id).orElseThrow();
        if (!seance.getReservations().isEmpty()){
            throw new IllegalStateException("Impossible de supprimer une séance avec des réservations");
        }
        seanceRepository.delete(seance);
    }
}
