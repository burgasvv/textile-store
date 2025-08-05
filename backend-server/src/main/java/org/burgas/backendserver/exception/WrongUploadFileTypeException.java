package org.burgas.backendserver.exception;

public class WrongUploadFileTypeException extends RuntimeException {

    public WrongUploadFileTypeException(String message) {
        super(message);
    }
}
