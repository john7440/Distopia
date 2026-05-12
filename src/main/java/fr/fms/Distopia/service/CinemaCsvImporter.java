package fr.fms.Distopia.service;

import fr.fms.Distopia.dao.CinemaRepository;
import fr.fms.Distopia.dao.TownRepository;
import fr.fms.Distopia.entities.Cinema;
import fr.fms.Distopia.entities.Town;
import fr.fms.Distopia.exceptions.ImportFailException;
import jakarta.transaction.Transactional;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CinemaCsvImporter {
    @Autowired
    private CinemaRepository cinemaRepository;
    @Autowired
    private TownRepository townRepository;

    @Value("classpath:data/cinemas.csv")
    private Resource csvFile;

    @Transactional
    public ImportResult importFromCsv() throws ImportFailException {
        int imported = 0;
        int skipped = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(csvFile.getInputStream(), StandardCharsets.UTF_8))) {

            CSVParser parser = CSVFormat.DEFAULT.builder()
                    .setDelimiter(';')
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreEmptyLines(true)
                    .setTrim(true)
                    .build()
                    .parse(reader);

            for (CSVRecord row : parser) {
                String name    = row.get("name");
                String comNom  = row.get("com_nom");

                if (name == null || name.isBlank() || cinemaRepository.existsByNameAndTown_Name(name, comNom)) {
                    skipped++;
                    continue; }

                Town town = townRepository.findByName(comNom)
                        .orElseGet(() -> townRepository.save(newTown(comNom)));

                Cinema cinema = new Cinema();
                cinema.setName(name);
                cinema.setTown(town);
                cinema.setWebsite(cleanUrl(row.get("website")));
                cinema.setLatitude(parseDouble(row.get("Y")));
                cinema.setLongitude(parseDouble(row.get("X")));

                cinemaRepository.save(cinema);
                imported++;
            }
        } catch (IOException e) {
            throw new ImportFailException("Erreur d'import csv: " + e.getMessage());
        }
        return new ImportResult(imported, skipped);
    }

    private Town newTown(String name) {
        Town t = new Town();
        t.setName(name != null ? name : "Inconnue");
        return t;
    }

    private String cleanUrl(String raw) {
        if (raw == null || raw.isBlank()) return null;
        Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(raw);
        return m.find() ? m.group(1) : raw;
    }

    private Double parseDouble(String val) {
        try { return (val != null && !val.isBlank()) ? Double.parseDouble(val) : null; }
        catch (NumberFormatException e) { return null; }
    }

    public record ImportResult(int imported, int skipped) {}
}

