package com.adoptapp.followupservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import com.adoptapp.followupservice.model.FollowUpStatus;

import java.time.LocalDateTime;

@Schema(description = "Resultado interno de un seguimiento post adopcion")
public record FollowUpResult(
        @Schema(description = "ID del registro", example = "1")
        Long id,
        @Schema(description = "Nombre del adoptante", example = "Juan Perez")
        String adopterName,
        @Schema(description = "Nombre de la mascota", example = "Charlie")
        String petName,
        @Schema(description = "ID del usuario asociado", example = "1")
        Long userId,
        @Schema(description = "ID de la mascota asociada", example = "10")
        Long petId,
        @Schema(description = "ID de la adopcion asociada", example = "5")
        Long adoptionId,
        @Schema(description = "Fecha y hora de la visita", example = "2026-05-01T10:00:00")
        LocalDateTime visitDate,
        @Schema(description = "Comentarios del registro", example = "Seguimiento realizado sin observaciones")
        String comments,
        @Schema(description = "Estado actual del registro", example = "ACTIVE")
        FollowUpStatus status,
        @Schema(description = "Fecha y hora de creacion del registro", example = "2026-04-21T14:30:00")
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
