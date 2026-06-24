package com.adoptapp.notificationservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import com.adoptapp.notificationservice.model.NotificationStatus;

import java.time.LocalDateTime;

@Schema(description = "Informacion de una notificacion")
public record NotificationResponse(

        @Schema(description = "ID del registro", example = "1")
        Long id,
        @Schema(description = "ID del usuario asociado", example = "1")
        Long userId,
        @Schema(description = "ID del refugio asociado", example = "2")
        Long shelterId,
        @Schema(description = "Destinatario de la notificacion", example = "usuario@mail.com")
        String recipient,
        @Schema(description = "Mensaje de la notificacion", example = "Registro creado correctamente")
        String message,
        @Schema(description = "ID del tipo de notificacion", example = "3")
        Long typeId,
        @Schema(description = "Tipo de notificacion", example = "PET_CREATED")
        String typeName,
        @Schema(description = "Estado actual del registro", example = "ACTIVE")
        NotificationStatus status,
        LocalDateTime createdAt
) {
}
