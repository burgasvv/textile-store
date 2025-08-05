package org.burgas.backendserver.message;

import lombok.Getter;

@Getter
public enum IdentityMessages {

    IDENTITY_IMAGE_NOT_FOUND("identity image not found"),
    IDENTITY_IMAGE_UPLOADED("Identity image uploaded"),
    IDENTITY_IMAGE_CHANGED("Identity image changed"),
    IDENTITY_IMAGE_DELETED("Identity image deleted"),
    IDENTITY_ENABLED("identity successfully enabled"),
    IDENTITY_DISABLED("Identity successfully disabled"),
    IDENTITY_ENABLE_MATCH("Identity enable match"),
    IDENTITY_PASSWORD_MATCHES("Identity password matches the new password"),
    IDENTITY_PASSWORD_CHANGED("identity password was changed"),
    IDENTITY_PASSWORD_EMPTY("Identity password is empty"),
    IDENTITY_NOT_AUTHENTICATED("Identity not authenticated"),
    IDENTITY_NOT_AUTHORIZED("identity not authorized"),
    IDENTITY_DELETED("Identity was successfully deleted"),
    IDENTITY_ID_EMPTY("Identity id empty"),
    IDENTITY_NOT_FOUND("Identity not found"),
    IDENTITY_FIELD_USERNAME_EMPTY("Identity field username is empty"),
    IDENTITY_FIELD_PASSWORD_EMPTY("Identity field password is empty"),
    IDENTITY_FIELD_EMAIL_EMPTY("Identity field email is empty"),
    IDENTITY_FIELD_PHONE_EMPTY("Identity field phone is empty");

    private final String message;

    IdentityMessages(String message) {
        this.message = message;
    }
}
