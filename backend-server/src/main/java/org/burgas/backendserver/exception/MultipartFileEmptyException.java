package org.burgas.backendserver.exception;

public class MultipartFileEmptyException extends RuntimeException {

    public MultipartFileEmptyException(String message) {
        super(message);
    }
}
