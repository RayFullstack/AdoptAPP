package com.adoptapp.donationservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import com.adoptapp.donationservice.model.DonationStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Informacion de una donacion")
public record DonationResponse(
        @Schema(description = "ID del registro", example = "1")
        Long id,
        @Schema(description = "Nombre del donante", example = "Camila Soto")
        String donorName,
        @Schema(description = "Monto asociado al registro", example = "25000")
        BigDecimal amount,
        @Schema(description = "Descripcion del registro", example = "Descripcion del registro")
        String description,
        @Schema(description = "Estado actual del registro", example = "ACTIVE")
        DonationStatus status,
        @Schema(description = "ID del usuario asociado", example = "1")
        Long userId,
        @Schema(description = "ID del refugio asociado", example = "2")
        Long shelterId,
        @Schema(description = "Fecha y hora de creacion del registro", example = "2026-04-21T14:30:00")
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
