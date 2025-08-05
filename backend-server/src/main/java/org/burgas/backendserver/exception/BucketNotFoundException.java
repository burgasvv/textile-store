package org.burgas.backendserver.exception;

public class BucketNotFoundException extends RuntimeException {

    public BucketNotFoundException(String message) {
        super(message);
    }
}
