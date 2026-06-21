package com.adoptapp.adoptionservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Schema(description = "Información de un cambio registrado en el historial de una adopción")
public record AdoptionHistoryResponse(
        @Schema(description = "ID del registro de la adopción", example = "3")
        Long id,

        @Schema(description = "ID de la adopción asociada al registro", example = "3")
        Long adoptionId,

        @Schema(description = "Acción realizada sobre la adopción", example = "UPDATE")
        String action,

        @Schema(description = "Descripción del cambio realizado", example = "La adopción cambió de PENDING a APPROVED")
        String description,

        @Schema(description = "Fecha  y hora en que se registró el cambio", example = "2026-04-21T14:30:00")
        LocalDateTime createdAt
) {
}
