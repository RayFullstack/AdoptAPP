package com.adoptapp.healthservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Informacion de un cambio registrado en el historial de salud")
public record HealthHistoryResponse(
        @Schema(description = "ID de la ficha de salud asociada", example = "8")
        Long healthId,

        @Schema(description = "Estado de esterilizacion anterior", example = "NOT_STERILIZED")
        String previousSterilizationStatus,

        @Schema(description = "Estado de esterilizacion nuevo", example = "STERILIZED")
        String newSterilizationStatus,

        @Schema(description = "Estado de vacunacion anterior", example = "NOT_VACCINATED")
        String previousVaccinationStatus,

        @Schema(description = "Estado de vacunacion nuevo", example = "VACCINATED")
        String newVaccinationStatus,

        @Schema(description = "Enfermedades anteriores registradas", example = "Sin enfermedades registradas")
        String previousDisease,

        @Schema(description = "Enfermedades nuevas registradas", example = "Alergia leve")
        String newDisease,

        @Schema(description = "Accion registrada en el historial", example = "UPDATED")
        String action,

        @Schema(description = "Fecha y hora del cambio registrado", example = "2026-04-21T14:30:00")
        LocalDateTime changedAt,

        @Schema(description = "Comentario del historial", example = "Registro actualizado")
        String comment,

        Long changedByUserId

) {
}
