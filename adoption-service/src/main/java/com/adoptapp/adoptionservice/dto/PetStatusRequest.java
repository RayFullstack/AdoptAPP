package com.adoptapp.adoptionservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Datos para crear el estado de la mascota")

public record PetStatusRequest(
        @Schema(description = "Nuevo estado de la mascota", example = "NOT_AVAILABLE",
                allowableValues = {"AVAILABLE", "NOT_AVAILABLE"})
        @NotBlank(message = "El estado de la mascota es requerido")
        String status
) {
}
