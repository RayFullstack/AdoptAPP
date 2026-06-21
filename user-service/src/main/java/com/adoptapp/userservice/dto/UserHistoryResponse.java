package com.adoptapp.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Información de un cambio registrado en el historial de un usuario")
public record UserHistoryResponse(
        @Schema(description = "ID del registro de  cambios del usuario", example = "3")
        Long id,

        @Schema(description = "ID del usuario", example = "3")
        Long userId,

        @Schema(description = "Nombre de pila anterior del usuario", example = "Juanita")
        String previousName,

        @Schema(description = "Nombre de pila  nuevo del usuario", example = "Juana")
        String newName,

        @Schema(description = "Apellido anterior del usuario", example = "Rios")
        String previousSurname,

        @Schema(description = "Apellido nuevo del usuario", example = "Araya")
        String newSurname,

        @Schema(description = "Nombre de usuario anterior ", example = "juanalaloca")
        String previousUsername,

        @Schema(description = "Nombre de usuario nuevo", example = "juanalasana")
        String newUsername,

        @Schema(description = "Correo electrónico anterior del usuario", example = "juana@mail.com")
        String previousEmail,

        @Schema(description = "Correo electrónico nuevo del usuario", example = "jane@mail.com")
        String newEmail,

        @Schema(description = "Número de teléfono anterior del usuario", example = "1223445567")
        String previousPhone,

        @Schema(description = "Número de teléfono  nuevo del usuario", example = "12344567")
        String newPhone,

        @Schema(description = "Estado anterior del usuario", example = "SUSPENDED",
        allowableValues = {"ACTIVE", "INACTIVE", "SUSPENDED"})
        String previousStatus,

        @Schema(description = "Estado nuevo del usuario", example = "ACTIVE",
                allowableValues = {"ACTIVE", "INACTIVE", "SUSPENDED"})
        String newStatus,

        @Schema(description = "Rol anterior del usuario", example = "ADOPTER",
                allowableValues = { "ADOPTER", "SHELTER_ADMIN", "VOLUNTEER", "VET", "ADMIN"})
        String previousRole,

        @Schema(description = "Rol nuevo del usuario", example = "VET",
                allowableValues = { "ADOPTER", "SHELTER_ADMIN", "VOLUNTEER", "VET", "ADMIN"})
        String newRole,

        @Schema(description = "Estado anterior del usuario", example = "true")
        Boolean previousActive,

        @Schema(description = "Estado nuevo del usuario", example = "false")
        Boolean newActive,

        @Schema(description = "Fecha y hora del cambio en el registro del usuario", example = "2026-04-21T14:30:00")
        LocalDateTime changedAt,

        @Schema(description = "Comentario asociado al cambio", example = "cambio realizado con exito")
        String comment
) {
}
