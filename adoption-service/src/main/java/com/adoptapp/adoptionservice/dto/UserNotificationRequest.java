package com.adoptapp.adoptionservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Datos para enviar una notificación relacionada con una adopción")
public record UserNotificationRequest(

        @Schema(description = "ID del usuario destinatario", example = "1")
        @NotNull(message = "El ID del usuario es requerido")
        Long userId,

        @Schema(description = "Correo electrónico del destinatario", example = "adopter@mail.com")
        @NotBlank(message = "El destinatario es requerido")
        @Email(message = "El correo del destinatario no es válido")
        String recipient,

        @Schema(description = "Mensaje de la notificación", example = "La solicitud de adopción fue creada correctamente")
        @NotBlank(message = "El mensaje es requerido")
        String message,

        @Schema(description = "Tipo de notificación", example = "ADOPTION_CREATED",
                allowableValues = {"ADOPTION_CREATED", "ADOPTION_UPDATED", "ADOPTION_DELETED"})
        @NotBlank(message = "El tipo de notificación es requerido")
        String typeName,

        @Schema(description = "Estado de envío de la notificación", example = "SENT",
                allowableValues = {"SENT", "PENDING", "FAILED", "ARCHIVED"})
        @NotBlank(message = "El estado de la notificación es requerido")
        String status
) {
}