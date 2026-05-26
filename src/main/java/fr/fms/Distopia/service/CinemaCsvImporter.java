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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service responsible for importing cinemas from a CSV file
 * <p>
 * This service reads cinema data from the configured CSV resource,
 * creates missing towns when necessary,
 * and stores imported cinemas in the database
 * <p>
 * Duplicate cinemas are ignored based on cinema name and town
 */
@Service
public class CinemaCsvImporter {

    private final CinemaRepository cinemaRepository;
    private final TownRepository townRepository;

    public CinemaCsvImporter(CinemaRepository cinemaRepository, TownRepository townRepository) {
        this.cinemaRepository = cinemaRepository;
        this.townRepository = townRepository;
    }

    @Value("classpath:data/cinemas.csv")
    private Resource csvFile;

    /**
     * Imports cinemas from the configured CSV file
     * <p>
     * For each valid row:<ul>
     *     <li>the town is extracted from the address</li>
     *     <li>a new town is created if necessary</li>
     *     <li>the cinema is saved in the database</li>
     * </ul>
     * <p>
     * Invalid or duplicate cinemas are skipped
     *
     * @return an {@link ImportResult} containing import statistics
     * @throws ImportFailException if the CSV file cannot be read
     */
    @Transactional
    public ImportResult importFromCsv() throws ImportFailException {
        int imported = 0;
        int skipped = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(csvFile.getInputStream(), StandardCharsets.UTF_8))) {

            CSVParser parser = CSVFormat.DEFAULT.builder()
                    .setDelimiter(',')
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreEmptyLines(true)
                    .setTrim(true)
                    .build()
                    .parse(reader);

            for (CSVRecord row : parser) {
                String name    = row.get("name");
                String address  = row.get("address");

                String comNom  = parseTownFromAddress(address);

                if (name == null || name.isBlank() || cinemaRepository.existsByNameAndTown_Name(name, comNom)) {
                    skipped++;
                    continue; }

                Town town = townRepository.findByName(comNom)
                        .orElseGet(() -> townRepository.save(newTown(comNom)));

                Cinema cinema = new Cinema();
                cinema.setName(name);
                cinema.setAddress(address);
                cinema.setWebsite(cleanUrl(row.get("website")));
                cinema.setLatitude(parseDouble(row.get("latitude")));
                cinema.setLongitude(parseDouble(row.get("longitude")));
                cinema.setImageUrl(row.isMapped("imageUrl") ? row.get("imageUrl") : null);
                cinema.setDepartment(row.isMapped("department") ? row.get("department") : null);
                cinema.setTown(town);

                cinemaRepository.save(cinema);
                imported++;
            }
        } catch (IOException e) {
            throw new ImportFailException("Erreur d'import csv: " + e.getMessage());
        }
        return new ImportResult(imported, skipped);
    }

    /**
     * Creates a new town entity
     * @param name the town name
     * @return the created town entity
     */
    private Town newTown(String name) {
        Town t = new Town();
        t.setName(name != null ? name : "Inconnue");
        return t;
    }

    /**
     * Extracts and cleans a website URL
     * <p>
     * If the raw value contains a URL inside parentheses,
     * only the URL part is returned
     * @param raw the raw website value
     * @return the cleaned URL, or null if the value is blank
     */
    private String cleanUrl(String raw) {
        if (raw == null || raw.isBlank()) return null;
        Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(raw);
        return m.find() ? m.group(1) : raw;
    }

    /**
     * Parses a string into a Double
     * @param val the string value to parse
     * @return the parsed double value, or null if parsing fails
     */
    private Double parseDouble(String val) {
        try { return (val != null && !val.isBlank()) ? Double.parseDouble(val) : null; }
        catch (NumberFormatException e) { return null; }
    }

    /**
     * Extracts the town name from an address
     * <p>
     * The method assumes the town is located
     * after the last comma in the address
     * and removes the postal code if present
     * @param address the full cinema address
     * @return the extracted town name, or "Inconnue" if unavailable
     */
    private String parseTownFromAddress(String address) {
        if (address == null || address.isBlank()) return "Inconnue";
        String[] parts = address.split(",");
        String last = parts[parts.length - 1].trim();
        return last.replaceFirst("^\\d{5}\\s*", "").trim();
    }

    /**
     * Import result statistics
     * @param imported the number of successfully imported cinemas
     * @param skipped the number of skipped cinemas
     */
    public record ImportResult(int imported, int skipped) {}
}

