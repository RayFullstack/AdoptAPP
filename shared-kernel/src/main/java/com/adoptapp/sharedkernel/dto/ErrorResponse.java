package com.adoptapp.sharedkernel.dto;

import java.time.LocalDateTime;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta estándar de error")
public record ErrorResponse(

        @Schema(description = "Fecha y hora en que ocurrió el error", example = "2026-06-20T14:30:00")
        LocalDateTime timestamp,

        @Schema(description = "Código de estado HTTP", example = "400")
        int status,

        @Schema(description = "Nombre del error HTTP", example = "Bad Request")
        String error,

        @Schema(description = "Descripción del error", example = "El petId es requerido")
        String message,

        @Schema(description = "Ruta donde ocurrió el error", example = "/adoptions")
        String path,

        @Schema(description = "Identificador utilizado para rastrear el error", example = "007a2396-4d29-4877-a26d-ac0d1fa9052b")
        String traceId,

        @Schema(description = "Lista de errores específicos de validación", example = "[\"El petId es requerido\"]")
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
