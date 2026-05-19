package com.adoptapp.sharedkernel.exception;

/**
 * Thrown when authentication fails.
 * Maps to HTTP 401 Unauthorized.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
