package org.burgas.backendserver.exception;

public class IdentityPasswordMatchesException extends RuntimeException {

    public IdentityPasswordMatchesException(String message) {
        super(message);
    }
}
