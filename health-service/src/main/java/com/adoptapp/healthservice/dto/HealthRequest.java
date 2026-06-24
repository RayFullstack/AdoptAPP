package com.adoptapp.healthservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import com.adoptapp.healthservice.model.SterilizationStatus;
import com.adoptapp.healthservice.model.VaccinationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Datos para crear o actualizar una ficha de salud")
public record HealthRequest(
        @NotNull
        @Schema(description = "ID del usuario asociado", example = "1")
        Long userId,

        @NotNull
        @Schema(description = "ID de la mascota asociada", example = "10")
        Long petId,

        @NotNull
        @Schema(description = "Estado de vacunacion", example = "VACCINATED")
        VaccinationStatus vaccinationStatus,

        @NotNull
        @Schema(description = "Estado de esterilizacion", example = "STERILIZED")
        SterilizationStatus sterilizationStatus,

        @NotBlank
        String diseases
) {
}
