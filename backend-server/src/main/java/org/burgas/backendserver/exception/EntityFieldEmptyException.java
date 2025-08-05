package org.burgas.backendserver.exception;

public class EntityFieldEmptyException extends RuntimeException {

    public EntityFieldEmptyException(String message) {
        super(message);
    }
}
