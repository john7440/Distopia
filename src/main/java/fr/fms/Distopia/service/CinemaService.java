package fr.fms.Distopia.service;

import fr.fms.Distopia.dao.CinemaRepository;
import fr.fms.Distopia.dao.TownRepository;
import fr.fms.Distopia.entities.Cinema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CinemaService {
    @Autowired
    private CinemaRepository cinemaRepository;
    @Autowired
    private TownRepository townRepository;

    //------affichage cinéma d'une ville------------------
    public List<Cinema> getByTown(Long townId) {
        return cinemaRepository.findByTownID(townId);
    }

}
