package org.burgas.backendserver.message;

import lombok.Getter;

@Getter
public enum BucketMessages {

    BUCKET_COOKIE_NOT_FOUND("Bucket cookie not found"),
    PRODUCT_ADDED_TO_BUCKET("Product added to bucket"),
    BUCKET_PRODUCT_NOT_FOUND("Bucket-Product not found"),
    BUCKET_NOT_FOUND("Bucket not found ");

    private final String messages;

    BucketMessages(String messages) {
        this.messages = messages;
    }
}
