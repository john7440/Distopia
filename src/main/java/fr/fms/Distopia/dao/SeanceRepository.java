package fr.fms.Distopia.dao;


import fr.fms.Distopia.entities.Seance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface SeanceRepository extends JpaRepository<Seance, Long> {
    List<Seance> findByMovieIdOrderByDateTimeAsc(Long movieId);
}
