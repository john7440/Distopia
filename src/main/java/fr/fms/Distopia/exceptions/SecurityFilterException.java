package fr.fms.Distopia.exceptions;

/**
 * Custom exception for the security filter
 */
public class SecurityFilterException extends RuntimeException {
    public SecurityFilterException(String message) {
        super(message);
    }
}
