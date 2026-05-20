package com.adoptapp.petservice.dto;

import com.adoptapp.petservice.model.SterilizationStatus;
import com.adoptapp.petservice.model.VaccinationStatus;

public record HealthRequest(
        Long userId,
        Long petId,
        VaccinationStatus vaccinationStatus,
        SterilizationStatus sterilizationStatus,
        String diseases
) {
}
