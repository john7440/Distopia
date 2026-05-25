package fr.fms.Distopia.exceptions;

/**
 * Exception thrown when a CSV import operation fails
 * <p>
 * Typically used during cinema import processing
 * when the CSV file cannot be read or parsed correctly
 */
public class ImportFailException extends RuntimeException {

    /**
     * Creates a new import failure exception
     *
     * @param message the detailed exception message
     */
    public ImportFailException(String message) {
        super(message);
    }
}
