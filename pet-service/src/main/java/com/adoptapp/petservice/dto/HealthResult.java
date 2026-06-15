package com.adoptapp.petservice.dto;

import java.time.LocalDateTime;

public record HealthResult(
        Long id,
        Long userId,
        Long petId,
        String vaccinationStatus,
        String sterilizationStatus,
        String diseases,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
