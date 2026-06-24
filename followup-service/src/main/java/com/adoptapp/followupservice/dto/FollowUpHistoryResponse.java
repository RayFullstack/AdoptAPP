package com.adoptapp.followupservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Informacion de un cambio registrado en el historial de seguimiento")
public record FollowUpHistoryResponse(
        @Schema(description = "ID del seguimiento asociado", example = "9")
        Long followUpId,
        @Schema(description = "Accion registrada en el historial", example = "UPDATED")
        String action,
        @Schema(description = "Estado anterior del registro", example = "PENDING")
        String previousStatus,
        @Schema(description = "Estado nuevo del registro", example = "APPROVED")
        String newStatus,
        @Schema(description = "Comentario del historial", example = "Registro actualizado")
        String comment,
        @Schema(description = "ID del usuario que realizo el cambio", example = "1")
        Long changedByUserId,
        LocalDateTime changedAt
) {
}
