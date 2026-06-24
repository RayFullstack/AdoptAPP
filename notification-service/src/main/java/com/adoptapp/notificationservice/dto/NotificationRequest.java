package com.adoptapp.notificationservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import com.adoptapp.notificationservice.model.NotificationStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Datos para crear una notificacion")
public record NotificationRequest(

        @Schema(description = "ID del usuario asociado", example = "1")
        Long userId,

        @Schema(description = "ID del refugio asociado", example = "2")
        Long shelterId,

        @NotBlank(message = "Recipient is required")
        @Schema(description = "Destinatario de la notificacion", example = "usuario@mail.com")
        String recipient,

        @NotBlank(message = "Message is required")
        @Schema(description = "Mensaje de la notificacion", example = "Registro creado correctamente")
        String message,

        @NotBlank(message = "Type name is required")
        @Schema(description = "Tipo de notificacion", example = "PET_CREATED")
        String typeName,

        @NotNull(message = "Status is required")
        NotificationStatus status
) {
}
