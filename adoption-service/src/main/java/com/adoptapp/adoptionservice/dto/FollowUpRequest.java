package com.adoptapp.adoptionservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Schema(description = "Datos para la creación de seguimiento de una adopción")
public record FollowUpRequest(
        @Schema(description = "Nombre del usuario adoptante ", example = "Juanito")
        @NotBlank(message = "El nombre del adoptante es requerido")
        String adopterName,

        @Schema(description = "Nombre de la mascota", example = "Charlie")
        @NotBlank(message = "El nombre de la mascota es requerido")
        String petName,

        @Schema(description = "ID del usuario adoptante ", example = "110")
        @NotNull(message = "El ID del usuario adoptante es requerido")
        Long userId,

        @Schema(description = "ID del usuario adoptante ", example = "33")
        @NotNull(message = "El ID de la mascota es requerido")
        Long petId,

        @Schema(description = "ID de la adopción", example = "56")
        @NotNull(message = "El ID de la adopción es requerido")
        Long adoptionId,

        @Schema(description = "Fecha de la visita ", example = "2026-04-21T14:30:00")
        @NotNull(message = "La fecha de visita es requerida")
        LocalDateTime visitDate,

        @Schema(description = "Comentarios de la visita de seguimiento",
                example = "Juanito está con sus vacunas al día y en su peso")
        String comments,

        @Schema(description = "Estado inicial del seguimiento post adopcion", example = "CANCELLED",
                allowableValues = {"PENDING","COMPLETED","CANCELLED"})
        @NotNull(message = "El estado del seguimiento es requerido")
        String status
) {
}
