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
import org.mockito.ArgumentCaptor;
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

    @Test
    @DisplayName("importFromCsv() - creates town when town does not exist")
    void importFromCsv_ShouldCreateTownWhenTownDoesNotExist() throws ImportFailException {
        String csv = """
                name,address,website,latitude,longitude
                Cinema Dax,"10 avenue test, 40100 Dax",https://cinema.fr,43.7,-1.0
                """;

        setCsvFile(csv);
        when(cinemaRepository.existsByNameAndTown_Name("Cinema Dax", "Dax")).thenReturn(false);
        when(townRepository.findByName("Dax")).thenReturn(Optional.empty());
        when(townRepository.save(any(Town.class))).thenAnswer(i-> i.getArgument(0));

        cinemaCsvImporter.importFromCsv();

        ArgumentCaptor<Town> townCaptor = ArgumentCaptor.forClass(Town.class);

        verify(townRepository).save(townCaptor.capture());
        assertThat(townCaptor.getValue().getName()).isEqualTo("Dax");
    }

    @Test
    @DisplayName("importFromCsv() - skips cinema when name is blank")
    void importFromCsv_ShouldSkipCinemaWhenNameIsBlank() throws ImportFailException {
        String csv = """
                name,address,website,latitude,longitude
                ,"10 avenue test, 40100 Dax",https://cinema.fr,43.7,-1.0
                """;

        setCsvFile(csv);

        CinemaCsvImporter.ImportResult result = cinemaCsvImporter.importFromCsv();

        assertThat(result.imported()).isZero();
        assertThat(result.skipped()).isEqualTo(1);
        verify(cinemaRepository, never()).save(any(Cinema.class));
    }

    @Test
    @DisplayName("importFromCsv() - skips cinema when cinema already exists in town")
    void importFromCsv_ShouldSkipCinemaWhenCinemaAlreadyExists() throws ImportFailException {
        String csv = """
                name,address,website,latitude,longitude
                Cinema Test,"10 avenue test, 40100 Dax",https://cinema.fr,43.7,-1.0
                """;
        setCsvFile(csv);

        when(cinemaRepository.existsByNameAndTown_Name("Cinema Test", "Dax")).thenReturn(true);

        CinemaCsvImporter.ImportResult result = cinemaCsvImporter.importFromCsv();

        assertThat(result.imported()).isZero();
        assertThat(result.skipped()).isEqualTo(1);
        verify(cinemaRepository, never()).save(any(Cinema.class));
    }

    @Test
    @DisplayName("importFromCsv() - cleans website url when value contains parentheses")
    void importFromCsv_ShouldCleanWebsiteUrlWhenValueContainsParentheses() throws ImportFailException {
        String csv = """
                name,address,website,latitude,longitude
                Cinema Test,"10 avenue test, 40100 Dax","Site officiel (https://cinema.fr)",43.7,-1.0
                """;
        setCsvFile(csv);

        Town town = new Town();
        town.setName("Dax");

        when(cinemaRepository.existsByNameAndTown_Name("Cinema Test", "Dax")).thenReturn(false);
        when(townRepository.findByName("Dax")).thenReturn(Optional.of(town));

        cinemaCsvImporter.importFromCsv();

        ArgumentCaptor<Cinema> cinemaCaptor = ArgumentCaptor.forClass(Cinema.class);

        verify(cinemaRepository).save(cinemaCaptor.capture());
        assertThat(cinemaCaptor.getValue().getWebsite()).isEqualTo("https://cinema.fr");
    }

    @Test
    @DisplayName("importFromCsv() - sets latitude and longitude to null when values are invalid")
    void importFromCsv_ShouldSetCoordinatesToNullWhenValuesAreInvalid() throws ImportFailException {
        String csv = """
                name,address,website,latitude,longitude
                Cinema Test,"10 avenue test, 40100 Dax",https://cinema.fr,wrong,bad
                """;

        setCsvFile(csv);

        Town town = new Town();
        town.setName("Dax");

        when(cinemaRepository.existsByNameAndTown_Name("Cinema Test", "Dax")).thenReturn(false);
        when(townRepository.findByName("Dax")).thenReturn(Optional.of(town));

        cinemaCsvImporter.importFromCsv();

        ArgumentCaptor<Cinema> cinemaCaptor = ArgumentCaptor.forClass(Cinema.class);

        verify(cinemaRepository).save(cinemaCaptor.capture());

        assertThat(cinemaCaptor.getValue().getLatitude()).isNull();
        assertThat(cinemaCaptor.getValue().getLongitude()).isNull();
    }

    @Test
    @DisplayName("importFromCsv() - uses Inconnue when address is blank")
    void importFromCsv_ShouldUseUnknownTownWhenAddressIsBlank() throws ImportFailException {
        String csv = """
                name,address,website,latitude,longitude
                Cinema Test,,https://cinema.fr,43.7,-1.0
                """;
        setCsvFile(csv);

        when(cinemaRepository.existsByNameAndTown_Name("Cinema Test", "Inconnue")).thenReturn(false);
        when(townRepository.findByName("Inconnue")).thenReturn(Optional.empty());
        when(townRepository.save(any(Town.class))).thenAnswer(invocation -> invocation.getArgument(0));

        cinemaCsvImporter.importFromCsv();

        ArgumentCaptor<Town> townCaptor = ArgumentCaptor.forClass(Town.class);

        verify(townRepository).save(townCaptor.capture());
        assertThat(townCaptor.getValue().getName()).isEqualTo("Inconnue");
    }

    //-------------------------helper------------------------
    private void setCsvFile(String csvContent) {
        ByteArrayResource resource = new ByteArrayResource(
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        ReflectionTestUtils.setField(cinemaCsvImporter, "csvFile", resource);
    }
}
