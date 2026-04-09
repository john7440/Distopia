package fr.fms.Distopia.exceptions;

/**
 * Custom exception for available seats
 */
public class NoSeatsAvailableException extends RuntimeException {
    public NoSeatsAvailableException(String message) {
        super(message);
    }
}
