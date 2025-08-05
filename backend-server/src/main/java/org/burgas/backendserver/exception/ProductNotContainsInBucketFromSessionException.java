package org.burgas.backendserver.exception;

public class ProductNotContainsInBucketFromSessionException extends RuntimeException {

    public ProductNotContainsInBucketFromSessionException(String message) {
        super(message);
    }
}
