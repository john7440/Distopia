package fr.fms.Distopia.exceptions;

public class NoSeatsAvailableException extends RuntimeException {
    public NoSeatsAvailableException(String message) {
        super(message);
    }
}
