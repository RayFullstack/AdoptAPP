package com.adoptapp.healthservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import com.adoptapp.healthservice.model.SterilizationStatus;
import com.adoptapp.healthservice.model.VaccinationStatus;

@Schema(description = "Datos internos para procesar una ficha de salud")
public record HealthCommand(
        @Schema(description = "ID del usuario asociado", example = "1")
        Long userId,
        @Schema(description = "ID de la mascota asociada", example = "10")
        Long petId,
        @Schema(description = "Estado de vacunacion", example = "VACCINATED")
        VaccinationStatus vaccinationStatus,
        @Schema(description = "Estado de esterilizacion", example = "STERILIZED")
        SterilizationStatus sterilizationStatus,
        String diseases
) {
}
