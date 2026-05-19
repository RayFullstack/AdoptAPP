package com.adoptapp.sharedkernel.exception;

/**
 * Thrown when a business rule is violated.
 * Maps to HTTP 400 Bad Request or 409 Conflict depending on context.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
