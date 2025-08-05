package org.burgas.backendserver.message;

import lombok.Getter;

@Getter
public enum BillMessages {

    BILL_PRODUCT_NOT_FOUND("Bill-Product not found"),
    BILL_NOT_FOUND("Bill not found");

    private final String message;

    BillMessages(String message) {
        this.message = message;
    }
}
