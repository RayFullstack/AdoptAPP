package com.adoptapp.staffservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Informacion de un cambio registrado en el historial de un trabajador")
public record StaffHistoryResponse(
        @Schema(description = "ID del trabajador asociado", example = "3")
        Long staffId,
        @Schema(description = "Accion registrada en el historial", example = "UPDATED")
        String action,
        @Schema(description = "Cargo anterior del trabajador", example = "VOLUNTEER")
        String previousPosition,
        @Schema(description = "Cargo nuevo del trabajador", example = "VET")
        String newPosition,
        @Schema(description = "Estado anterior del registro", example = "PENDING")
        String previousStatus,
        @Schema(description = "Estado nuevo del registro", example = "APPROVED")
        String newStatus,
        @Schema(description = "Telefono anterior", example = "912345678")
        String previousPhone,
        @Schema(description = "Telefono nuevo", example = "987654321")
        String newPhone,
        @Schema(description = "Comentario del historial", example = "Registro actualizado")
        String comment,
        @Schema(description = "ID del usuario que realizo el cambio", example = "1")
        Long changedByUserId,
        LocalDateTime changedAt
) {
}
