package com.adoptapp.sharedkernel.exception;

/**
 * Thrown when the user lacks required permissions.
 * Maps to HTTP 403 Forbidden.
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
