package com.adoptapp.sharedkernel.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standard error response shared across all microservices.
 * Provides consistent JSON structure for error responses.
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        String traceId,
        List<String> details
) {

    public ErrorResponse(int status, String error, String message) {
        this(LocalDateTime.now(), status, error, message, null, null, null);
    }

    public ErrorResponse(int status, String error, String message, String path) {
        this(LocalDateTime.now(), status, error, message, path, null, null);
    }

    public ErrorResponse(int status, String error, String message, String path, String traceId) {
        this(LocalDateTime.now(), status, error, message, path, traceId, null);
    }

    public ErrorResponse(int status, String error, String message, List<String> details) {
        this(LocalDateTime.now(), status, error, message, null, null, details);
    }

    public ErrorResponse(int status, String error, String message, String path, String traceId, List<String> details) {
        this(LocalDateTime.now(), status, error, message, path, traceId, details);
    }
}
