package org.burgas.backendserver.message;

import lombok.Getter;

@Getter
public enum ProductMessages {

    PRODUCT_CONTAINS_IN_BUCKET("Product already contains in bucket"),
    PRODUCT_NOT_CONTAINS_IN_BUCKET("Product not contains in bucket"),
    WRONG_PRODUCT_AMOUNT("Wrong product amount (0 or below)"),
    PRODUCT_DELETED("Product successfully deleted"),
    PRODUCT_ID_EMPTY("Product id is empty"),
    PRODUCT_NOT_FOUND("Product not found"),
    PRODUCT_ENTITY_FIELD_NAME_EMPTY("Product entity field name is empty"),
    PRODUCT_ENTITY_FIELD_DESCRIPTION_EMPTY("Product entity field description is empty"),
    PRODUCT_ENTITY_FIELD_PRICE_EMPTY("Product entity field price is empty"),
    PRODUCT_ENTITY_FIELD_CATEGORY_EMPTY("Product entity field category is empty");

    private final String message;

    ProductMessages(String message) {
        this.message = message;
    }
}
