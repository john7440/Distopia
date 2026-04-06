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

    //-----------------rechercher par mot-clé (nom ou adresse)-------------------------
    public List<Cinema> search(String keyword){
        return cinemaRepository.findByNameContainingIgnoreCaseOrAddressContainingIgnoreCase(keyword,keyword);
    }

    //-----------afficher tous les cinémas-------------
    public List<Cinema> getAll(){
        return cinemaRepository.findAll();
    }

    //-----------------------créer ou modifier un cinéma----------------
    public Cinema save(Long id, String name, String address, Long townId){
        Cinema cinema = (id != null) ? cinemaRepository.findById(id).orElse(new Cinema()): new Cinema();
        cinema.setName(name);
        cinema.setAddress(address);
        if (townId != null){
            townRepository.findById(townId).ifPresent(cinema::setTown);
        }
        return cinemaRepository.save(cinema);
    }

    //--------------supprimer un conéma----------------
    public void delete(Long id){
        cinemaRepository.deleteById(id);
    }
}
