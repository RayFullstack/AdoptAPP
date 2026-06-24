package com.adoptapp.followupservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import com.adoptapp.followupservice.model.FollowUpStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Schema(description = "Datos para crear o actualizar un seguimiento post adopcion")
public record FollowUpRequest(

        @NotBlank(message = "Adopter name is required")
        @Schema(description = "Nombre del adoptante", example = "Juan Perez")
        String adopterName,

        @NotBlank(message = "Pet name is required")
        @Schema(description = "Nombre de la mascota", example = "Charlie")
        String petName,

        @Schema(description = "ID del usuario asociado", example = "1")
        Long userId,

        @Schema(description = "ID de la mascota asociada", example = "10")
        Long petId,

        @Schema(description = "ID de la adopcion asociada", example = "5")
        Long adoptionId,

        @NotNull(message = "Visit date is required")
        @Schema(description = "Fecha y hora de la visita", example = "2026-05-01T10:00:00")
        LocalDateTime visitDate,

        @Schema(description = "Comentarios del registro", example = "Seguimiento realizado sin observaciones")
        String comments,

        @NotNull(message = "Status is required")
        FollowUpStatus status
) {
}
