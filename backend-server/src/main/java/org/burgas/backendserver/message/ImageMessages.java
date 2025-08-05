package org.burgas.backendserver.message;

import lombok.Getter;

@Getter
public enum ImageMessages {

    WRONG_UPLOAD_FILE_TYPE("Wrong upload file type"),
    IMAGE_DELETED("Image successfully deleted"),
    MULTIPART_FILE_EMPTY("Multipart file is empty"),
    IMAGE_NOT_FOUND("Image not found");

    private final String message;

    ImageMessages(String message) {
        this.message = message;
    }
}
