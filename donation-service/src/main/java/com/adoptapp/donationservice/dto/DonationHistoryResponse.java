package com.adoptapp.donationservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Informacion de un cambio registrado en el historial de una donacion")
public record DonationHistoryResponse(

        @Schema(description = "ID de la donacion asociada", example = "4")
        Long donationId,

        @Schema(description = "Accion registrada en el historial", example = "UPDATED")
        String action,

        @Schema(description = "Estado anterior del registro", example = "PENDING")
        String previousStatus,

        @Schema(description = "Estado nuevo del registro", example = "APPROVED")
        String newStatus,

        @Schema(description = "Monto anterior de la donacion", example = "10000")
        BigDecimal previousAmount,

        @Schema(description = "Monto nuevo de la donacion", example = "15000")
        BigDecimal newAmount,

        @Schema(description = "Comentario del historial", example = "Registro actualizado")
        String comment,

        @Schema(description = "Fecha y hora del cambio registrado", example = "2026-04-21T14:30:00")
        LocalDateTime changedAt,

        Long changedByUserId
) {
}
