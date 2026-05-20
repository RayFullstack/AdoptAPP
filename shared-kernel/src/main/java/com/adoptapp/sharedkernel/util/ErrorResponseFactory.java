package com.adoptapp.sharedkernel.util;

import com.adoptapp.sharedkernel.dto.ErrorResponse;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.UUID;

/**
 * Factory for creating standardized ErrorResponse instances.
 * Eliminates duplication of error response construction across microservices.
 */
public final class ErrorResponseFactory {

    private ErrorResponseFactory() {
    }

    public static ErrorResponse badRequest(String message) {
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Bad Request", message);
    }

    public static ErrorResponse badRequest(String message, String path) {
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Bad Request", message, path, resolveTraceId());
    }

    public static ErrorResponse badRequest(List<String> details) {
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Bad Request", "Error de validación", details);
    }

    public static ErrorResponse badRequest(List<String> details, String path) {
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Bad Request", "Error de validación", path, resolveTraceId(), details);
    }

    public static ErrorResponse notFound(String message) {
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Not Found", message);
    }

    public static ErrorResponse notFound(String message, String path) {
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Not Found", message, path, resolveTraceId());
    }

    public static ErrorResponse conflict(String message) {
        return new ErrorResponse(HttpStatus.CONFLICT.value(), "Conflict", message);
    }

    public static ErrorResponse conflict(String message, String path) {
        return new ErrorResponse(HttpStatus.CONFLICT.value(), "Conflict", message, path, resolveTraceId());
    }

    public static ErrorResponse unauthorized(String message) {
        return new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", message);
    }

    public static ErrorResponse unauthorized(String message, String path) {
        return new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", message, path, resolveTraceId());
    }

    public static ErrorResponse forbidden(String message) {
        return new ErrorResponse(HttpStatus.FORBIDDEN.value(), "Forbidden", message);
    }

    public static ErrorResponse forbidden(String message, String path) {
        return new ErrorResponse(HttpStatus.FORBIDDEN.value(), "Forbidden", message, path, resolveTraceId());
    }

    public static ErrorResponse serviceUnavailable(String serviceName) {
        return new ErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Service Unavailable",
                "El servicio " + serviceName + " no está disponible"
        );
    }

    public static ErrorResponse serviceUnavailable(String serviceName, String path) {
        return new ErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Service Unavailable",
                "El servicio " + serviceName + " no está disponible",
                path,
                resolveTraceId()
        );
    }

    public static ErrorResponse internalServerError() {
        return new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Error interno del servidor"
        );
    }

    public static ErrorResponse internalServerError(String message) {
        return new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                message
        );
    }

    public static ErrorResponse internalServerError(String message, String path) {
        return new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                message,
                path,
                resolveTraceId()
        );
    }

    private static String resolveTraceId() {
        return UUID.randomUUID().toString();
    }
}
