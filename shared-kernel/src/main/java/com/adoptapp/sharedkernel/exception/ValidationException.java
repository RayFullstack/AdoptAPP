package com.adoptapp.sharedkernel.exception;

/**
 * Thrown when validation fails.
 * Maps to HTTP 400 Bad Request.
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
