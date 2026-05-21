package fr.fms.Distopia.service;

import fr.fms.Distopia.dao.CinemaRepository;
import fr.fms.Distopia.dao.TownRepository;
import fr.fms.Distopia.entities.Cinema;
import fr.fms.Distopia.entities.Town;
import fr.fms.Distopia.exceptions.ImportFailException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CinemaCsvImporterTest {

    private CinemaCsvImporter cinemaCsvImporter;

    private CinemaRepository cinemaRepository;
    private TownRepository townRepository;

    @BeforeEach
    void setUp() {
        cinemaRepository = mock(CinemaRepository.class);
        townRepository = mock(TownRepository.class);

        cinemaCsvImporter = new CinemaCsvImporter();

        ReflectionTestUtils.setField(cinemaCsvImporter, "cinemaRepository", cinemaRepository);
        ReflectionTestUtils.setField(cinemaCsvImporter, "townRepository", townRepository);
    }

    //---------------------tests for importFromCsv() ----------------------------------
    @Test
    @DisplayName("importFromCsv() - imports cinema when csv row is valid")
    void importFromCsv_ShouldImportCinemaWhenRowIsValid() throws ImportFailException {
        String csv = """
                name,address,website,latitude,longitude,imageUrl,department
                Cinema Test,"12 rue test, 40100 Dax","Site (https://cinema.fr)",43.7102,-1.0536,/img/cinema.jpg,40
                """;
        setCsvFile(csv);

        Town town = new Town();
        town.setName("Dax");

        when(cinemaRepository.existsByNameAndTown_Name("Cinema Test", "Dax")).thenReturn(false);
        when(townRepository.findByName("Dax")).thenReturn(Optional.of(town));

        CinemaCsvImporter.ImportResult result = cinemaCsvImporter.importFromCsv();

        assertThat(result.imported()).isEqualTo(1);
        assertThat(result.skipped()).isZero();

        verify(cinemaRepository).save(any(Cinema.class));
    }

    //-------------------------helper------------------------
    private void setCsvFile(String csvContent) {
        ByteArrayResource resource = new ByteArrayResource(
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        ReflectionTestUtils.setField(cinemaCsvImporter, "csvFile", resource);
    }
}
