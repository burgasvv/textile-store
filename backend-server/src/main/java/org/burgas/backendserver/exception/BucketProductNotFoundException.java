package org.burgas.backendserver.exception;

public class BucketProductNotFoundException extends RuntimeException {

    public BucketProductNotFoundException(String message) {
        super(message);
    }
}
