package com.adoptapp.healthservice.dto;

import com.adoptapp.healthservice.model.SterilizationStatus;
import com.adoptapp.healthservice.model.VaccinationStatus;
import com.adoptapp.healthservice.model.HealthStatus;

import java.time.LocalDateTime;

public record HealthResponse(
        Long id,
        Long userId,
        Long petId,
        VaccinationStatus vaccinationStatus,
        SterilizationStatus sterilizationStatus,
        String diseases,
        HealthStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
