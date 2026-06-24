package com.adoptapp.followupservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos para crear una notificacion")
public record NotificationRequest(
        @Schema(description = "ID del usuario asociado", example = "1")
        Long userId,
        @Schema(description = "ID del refugio asociado", example = "2")
        Long shelterId,
        @Schema(description = "Destinatario de la notificacion", example = "usuario@mail.com")
        String recipient,
        @Schema(description = "Mensaje de la notificacion", example = "Registro creado correctamente")
        String message,
        @Schema(description = "Tipo de notificacion", example = "PET_CREATED")
        String typeName,
        String status
) {
}
