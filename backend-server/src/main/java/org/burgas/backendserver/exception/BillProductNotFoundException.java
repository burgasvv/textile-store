package org.burgas.backendserver.exception;

public class BillProductNotFoundException extends RuntimeException {

    public BillProductNotFoundException(String message) {
        super(message);
    }
}
