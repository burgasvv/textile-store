package org.burgas.backendserver.exception;

public class ProductContainsInBucketFromSessionException extends RuntimeException {

    public ProductContainsInBucketFromSessionException(String message) {
        super(message);
    }
}
