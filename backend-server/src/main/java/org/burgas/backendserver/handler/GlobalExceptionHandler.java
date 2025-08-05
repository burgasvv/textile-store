package org.burgas.backendserver.handler;

import org.burgas.backendserver.exception.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.charset.StandardCharsets;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolationException(DataIntegrityViolationException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_ACCEPTABLE)
                .contentType(new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8))
                .body(exception.getMessage());
    }

    @ExceptionHandler(EntityFieldEmptyException.class)
    public ResponseEntity<String> handleEntityFieldEmptyException(EntityFieldEmptyException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_ACCEPTABLE)
                .contentType(new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8))
                .body(exception.getMessage());
    }

    @ExceptionHandler(IdentityNotFoundException.class)
    public ResponseEntity<String> handleIdentityNotFoundException(IdentityNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8))
                .body(exception.getMessage());
    }

    @ExceptionHandler(IdEmptyException.class)
    public ResponseEntity<String> handleIdentityIdEmptyException(IdEmptyException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_ACCEPTABLE)
                .contentType(new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8))
                .body(exception.getMessage());
    }

    @ExceptionHandler(IdentityNotAuthenticatedException.class)
    public ResponseEntity<String> handleIdentityNotAuthenticatedException(IdentityNotAuthenticatedException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_ACCEPTABLE)
                .contentType(new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8))
                .body(exception.getMessage());
    }

    @ExceptionHandler(IdentityNotAuthorizedException.class)
    public ResponseEntity<String> handleIdentityNotAuthorizedException(IdentityNotAuthorizedException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_ACCEPTABLE)
                .contentType(new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8))
                .body(exception.getMessage());
    }

    @ExceptionHandler(IdentityPasswordEmptyException.class)
    public ResponseEntity<String> handleIdentityPasswordEmptyException(IdentityPasswordEmptyException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_ACCEPTABLE)
                .contentType(new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8))
                .body(exception.getMessage());
    }

    @ExceptionHandler(IdentityPasswordMatchesException.class)
    public ResponseEntity<String> handleIdentityPasswordMatchesException(IdentityPasswordMatchesException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_ACCEPTABLE)
                .contentType(new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8))
                .body(exception.getMessage());
    }

    @ExceptionHandler(IdentityEnableMatchException.class)
    public ResponseEntity<String> handleIdentityEnableMatchException(IdentityEnableMatchException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_ACCEPTABLE)
                .contentType(new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8))
                .body(exception.getMessage());
    }

    @ExceptionHandler(ImageNotFoundException.class)
    public ResponseEntity<String> handleImageNotFoundException(ImageNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8))
                .body(exception.getMessage());
    }

    @ExceptionHandler(MultipartFileEmptyException.class)
    public ResponseEntity<String> handleMultipartFileEmptyException(MultipartFileEmptyException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8))
                .body(exception.getMessage());
    }

    @ExceptionHandler(WrongUploadFileTypeException.class)
    public ResponseEntity<String> handleWrongUploadFileTypeException(WrongUploadFileTypeException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_ACCEPTABLE)
                .contentType(new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8))
                .body(exception.getMessage());
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<String> handleCategoryNotFoundException(CategoryNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8))
                .body(exception.getMessage());
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<String> handleProductNotFoundException(ProductNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8))
                .body(exception.getMessage());
    }

    @ExceptionHandler(BucketNotFoundException.class)
    public ResponseEntity<String> handleBucketNotFoundException(BucketNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8))
                .body(exception.getMessage());
    }

    @ExceptionHandler(BucketProductNotFoundException.class)
    public ResponseEntity<String> handleBucketProductNotFoundException(BucketProductNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8))
                .body(exception.getMessage());
    }

    @ExceptionHandler(WrongProductAmountException.class)
    public ResponseEntity<String> handleWrongProductAmountException(WrongProductAmountException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_ACCEPTABLE)
                .contentType(new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8))
                .body(exception.getMessage());
    }

    @ExceptionHandler(ProductNotContainsInBucketFromSessionException.class)
    public ResponseEntity<String> handleProductNotContainsInBucketFromSession(ProductNotContainsInBucketFromSessionException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8))
                .body(exception.getMessage());
    }

    @ExceptionHandler(ProductContainsInBucketFromSessionException.class)
    public ResponseEntity<String> handleProductContainsInBucketFromSession(ProductContainsInBucketFromSessionException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_ACCEPTABLE)
                .contentType(new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8))
                .body(exception.getMessage());
    }

    @ExceptionHandler(BucketCookieNotFoundException.class)
    public ResponseEntity<String> handleBucketCookieNotFoundException(BucketCookieNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8))
                .body(exception.getMessage());
    }

    @ExceptionHandler(BillNotFoundException.class)
    public ResponseEntity<String> handleBillNotFoundException(BillNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8))
                .body(exception.getMessage());
    }

    @ExceptionHandler(BillProductNotFoundException.class)
    public ResponseEntity<String> handleBillProductNotFoundException(BillProductNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(new MediaType(MediaType.TEXT_PLAIN, StandardCharsets.UTF_8))
                .body(exception.getMessage());
    }
}
