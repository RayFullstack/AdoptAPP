package com.adoptapp.healthservice.dto;

import com.adoptapp.healthservice.model.SterilizationStatus;
import com.adoptapp.healthservice.model.VaccinationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record HealthRequest(
        @NotNull
        Long userId,

        @NotNull
        Long petId,

        @NotNull
        VaccinationStatus vaccinationStatus,

        @NotNull
        SterilizationStatus sterilizationStatus,

        @NotBlank
        String diseases
) {
}
