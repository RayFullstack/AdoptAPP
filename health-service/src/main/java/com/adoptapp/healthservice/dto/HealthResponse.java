package com.adoptapp.healthservice.dto;

import com.adoptapp.healthservice.model.SterilizationStatus;
import com.adoptapp.healthservice.model.VaccinationStatus;

import java.time.LocalDateTime;

public record HealthResponse(
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
