package com.adoptapp.healthservice.dto;

import com.adoptapp.healthservice.model.SterilizationStatus;
import com.adoptapp.healthservice.model.VaccinationStatus;

public record HealthCommand(
        Long userId,
        Long petId,
        VaccinationStatus vaccinationStatus,
        SterilizationStatus sterilizationStatus,
        String diseases
) {
}
