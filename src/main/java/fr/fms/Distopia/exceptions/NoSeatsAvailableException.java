package fr.fms.Distopia.exceptions;

/**
 * Exception thrown when a reservation cannot be completed
 * because no seats remain available for a seance
 */
public class NoSeatsAvailableException extends RuntimeException {

    /**
     * Creates a new no seats available exception
     * @param message the detailed exception message
     */
    public NoSeatsAvailableException(String message) {
        super(message);
    }
}
