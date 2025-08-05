package org.burgas.backendserver.exception;

public class IdentityPasswordEmptyException extends RuntimeException {

    public IdentityPasswordEmptyException(String message) {
        super(message);
    }
}
