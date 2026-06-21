package com.adoptapp.adoptionservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Datos para la creación de una notificación")
public record PetNotificationRequest(
        @Schema(description = "Id del usuario adoptante ", example = "11")
        @NotNull(message = "El ID del adoptante es requerido")
        Long userId,

        @Schema(description = "Destinatario de la notifcacion ", example = "adopter@mail.com")
        @NotBlank(message = "El nombre del adoptante es requerido")
        String recipient,

        @Schema(description = "Mensaje de la notificación ", example = "Adopción creada correctamente")
        @NotBlank(message = "El mensaje es requerido")
        String message,

        @Schema(description = "Nombre del usuario adoptante ", example = "PET_CREATED",
                allowableValues = {"PET_CREATED", "PET_UPDATED", "PET_DELETED", "PET_STATUS_CHANGED"})
        @NotBlank(message = "El tipo de notificación es requerido")
        String typeName,

        @Schema(description = "Estado actual de la notificación ", example = "SENT",
        allowableValues = {"SENT", "PENDING", "FAILED", "ARCHIVED"})
        @NotBlank(message = "El estado de la notificación es requerido")
        String status
) {
}
