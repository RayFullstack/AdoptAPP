package com.adoptapp.petservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Informacion de un cambio registrado en el historial de una mascota")
public record PetHistoryResult(
    @Schema(description = "ID del registro", example = "1")
    Long id,
    @Schema(description = "ID de la mascota asociada", example = "10")
    Long petId,
    @Schema(description = "Nombre anterior del registro", example = "Refugio Norte")
    String previousName,
    @Schema(description = "Nombre nuevo del registro", example = "Refugio Central")
    String newName,
    @Schema(description = "Estado anterior del registro", example = "PENDING")
    String previousStatus,
    @Schema(description = "Estado nuevo del registro", example = "APPROVED")
    String newStatus,
    @Schema(description = "ID del usuario que realizo el cambio", example = "1")
    Long changedByUserId,
    @Schema(description = "Fecha y hora del cambio registrado", example = "2026-04-21T14:30:00")
    LocalDateTime changedAt,
    String comment
) {
}
