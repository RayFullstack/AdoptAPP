package com.adoptapp.petservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Resultado interno de una ficha de salud")
public record HealthResult(
        @Schema(description = "ID del registro", example = "1")
        Long id,
        @Schema(description = "ID del usuario asociado", example = "1")
        Long userId,
        @Schema(description = "ID de la mascota asociada", example = "10")
        Long petId,
        @Schema(description = "Estado de vacunacion", example = "VACCINATED")
        String vaccinationStatus,
        @Schema(description = "Estado de esterilizacion", example = "STERILIZED")
        String sterilizationStatus,
        @Schema(description = "Enfermedades o antecedentes medicos", example = "Sin enfermedades registradas")
        String diseases,
        @Schema(description = "Fecha y hora de creacion del registro", example = "2026-04-21T14:30:00")
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
