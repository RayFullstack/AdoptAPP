package com.adoptapp.notificationservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import com.adoptapp.notificationservice.model.NotificationStatus;

@Schema(description = "Datos internos para procesar una notificacion")
public record NotificationCommand(

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
        NotificationStatus status
) {
}
