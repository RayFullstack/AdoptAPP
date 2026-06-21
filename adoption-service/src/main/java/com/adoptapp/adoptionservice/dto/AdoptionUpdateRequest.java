package com.adoptapp.adoptionservice.dto;

import com.adoptapp.adoptionservice.model.AdoptionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Datos para actualizar el estado de una solicitud de adopción")
public record AdoptionUpdateRequest(
        @Schema(
                description = "Nuevo estado para la adopción. Solo permite aprobar o rechazar una solicitud pendiente",
                example = "APPROVED",
                allowableValues = {"APPROVED", "REJECTED"},
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "El estado de la adopción es requerido")
        AdoptionStatus status
) {
}
