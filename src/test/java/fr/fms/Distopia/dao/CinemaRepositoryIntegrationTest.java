package fr.fms.Distopia.dao;

import fr.fms.Distopia.entities.Cinema;
import fr.fms.Distopia.entities.Town;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for cinema repository queries
 *
 * <p>Uses the H2 test database to verify that cinema existence checks respect soft-delete lifecycle rules</p>
 */
@DataJpaTest
@ActiveProfiles("test")
class CinemaRepositoryIntegrationTest {

    @Autowired
    private CinemaRepository cinemaRepository;

    @Autowired
    private TownRepository townRepository;

    @Test
    @DisplayName("existsByNameAndTown_Name() - includes deleted cinemas")
    void existsByNameAndTown_ShouldIncludeDeletedCinema() {

        Town town = new Town();
        town.setName("Dax");
        town = townRepository.saveAndFlush(town);

        Cinema cinema = new Cinema();
        cinema.setName("Cinema Test");
        cinema.setAddress("10 avenue du test");
        cinema.setTown(town);
        cinema.setDeleted(true);

        cinemaRepository.saveAndFlush(cinema);

        assertThat(cinemaRepository.existsByNameAndTown_Name("Cinema Test", "Dax")).isTrue();
        assertThat(cinemaRepository.existsByNameAndTown_NameAndDeletedFalse("Cinema Test", "Dax")).isFalse();
        assertThat(cinemaRepository.existsByNameAndTown_Name("Cinema Test", "Bayonne")).isFalse();
    }
}
