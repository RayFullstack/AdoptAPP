package com.adoptapp.petservice.dto;

import com.adoptapp.petservice.model.SterilizationStatus;
import com.adoptapp.petservice.model.VaccinationStatus;

import java.time.LocalDateTime;

public record HealthResult(
        Long id,
        Long userId,
        Long petId,
        VaccinationStatus vaccinationStatus,
        SterilizationStatus sterilizationStatus,
        String diseases,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
