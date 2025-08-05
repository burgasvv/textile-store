package org.burgas.backendserver.message;

import lombok.Getter;

@Getter
public enum CategoryMessages {

    CATEGORY_DELETED("Category successfully deleted"),
    CATEGORY_ID_EMPTY("Category id empty"),
    MULTIPLE_CATEGORIES_CREATED("Multiple categories created"),
    CATEGORY_NOT_FOUND("Category not found"),
    CATEGORY_FIELD_NAME_EMPTY("Category field name is empty");

    private final String message;

    CategoryMessages(String message) {
        this.message = message;
    }
}
