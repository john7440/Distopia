package fr.fms.Distopia.service;

import fr.fms.Distopia.dao.CinemaRepository;
import fr.fms.Distopia.dao.TownRepository;
import fr.fms.Distopia.entities.Town;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TownService {

    @Autowired
    private TownRepository townRepository;

    @Autowired
    private CinemaRepository cinemaRepository;

    //----------------------methode pour trouver toutes les villes-----------------------
    public List<Town> getAll() {
        return townRepository.findAll();
    }

    //------------------méthode pour sauvegarder une ville----------------------------
    public Town save(Long id, String name) {
        Town town = (id != null) ? townRepository.findById(id).orElse(new Town()) : new Town();
        town.setName(name);
        return townRepository.save(town);
    }

    //--------méthode pour supprimer une ville (avec modif de la ville des cinema impactés au besoin)------------
    @Transactional
    public void delete(Long id) {
        townRepository.findById(id).ifPresent(town -> {
            town.getCinemas().forEach(cinema -> {
                cinema.setTown(null);
                cinemaRepository.save(cinema);
            });
            townRepository.delete(town);
        });
    }

}
