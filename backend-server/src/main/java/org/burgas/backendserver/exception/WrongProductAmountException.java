package org.burgas.backendserver.exception;

public class WrongProductAmountException extends RuntimeException {

    public WrongProductAmountException(String message) {
        super(message);
    }
}
