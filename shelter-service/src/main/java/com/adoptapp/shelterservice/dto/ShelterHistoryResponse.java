package com.adoptapp.shelterservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Informacion de un cambio registrado en el historial de un refugio")
public record ShelterHistoryResponse(

        @Schema(description = "ID del refugio asociado", example = "2")
        Long shelterId,

        @Schema(description = "Accion registrada en el historial", example = "UPDATED")
        String action,

        @Schema(description = "Nombre anterior del registro", example = "Refugio Norte")
        String previousName,

        @Schema(description = "Nombre nuevo del registro", example = "Refugio Central")
        String newName,

        @Schema(description = "Correo electronico anterior", example = "anterior@mail.com")
        String previousEmail,

        @Schema(description = "Correo electronico nuevo", example = "nuevo@mail.com")
        String newEmail,

        @Schema(description = "Telefono anterior", example = "912345678")
        String previousPhone,

        @Schema(description = "Telefono nuevo", example = "987654321")
        String newPhone,

        @Schema(description = "Descripcion anterior", example = "Descripcion anterior")
        String previousDescription,

        @Schema(description = "Descripcion nueva", example = "Descripcion actualizada")
        String newDescription,

        @Schema(description = "Estado anterior del registro", example = "PENDING")
        String previousStatus,

        @Schema(description = "Estado nuevo del registro", example = "APPROVED")
        String newStatus,

        @Schema(description = "Estado activo anterior", example = "true")
        Boolean previousActive,

        @Schema(description = "Estado activo nuevo", example = "false")
        Boolean newActive,

        @Schema(description = "Comentario del historial", example = "Registro actualizado")
        String comment,

        @Schema(description = "ID del usuario que realizo el cambio", example = "1")
        Long changedByUserId,

        LocalDateTime changedAt
) {
}
