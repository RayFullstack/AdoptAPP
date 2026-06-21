package com.adoptapp.adoptionservice.dto;

import com.adoptapp.adoptionservice.model.AdoptionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Información de una adopción")
public record AdoptionResponse(

        @Schema(description = "ID de la adopción", example = "4")
        Long id,

        @Schema(description = "ID del usuario adoptante", example = "7")
        Long userId,

        @Schema(description = "ID de la mascota adoptada", example = "15")
        Long petId,

        @Schema(description = "Status de un cambio registrado en el historial de una adopción", example = "PENDING",
                allowableValues = {"PENDING", "APPROVED", "REJECTED", "CANCELLED"})
        AdoptionStatus status,

        @Schema(description = "Fecha de la creación del estado de la adopción", example = "2026-04-21T14:30:00")
        LocalDateTime createdAt,

        @Schema(description = "Fecha de la actualización del estado de la adopción", example = "2026-04-21T14:30:00",
                allowableValues = {
                        "PENDING",
                        "APPROVED",
                        "REJECTED",
                        "CANCELLED"
                })
        LocalDateTime updatedAt) {
                }
