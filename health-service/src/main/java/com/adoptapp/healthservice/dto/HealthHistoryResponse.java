package com.adoptapp.healthservice.dto;

import java.time.LocalDateTime;

public record HealthHistoryResponse(
        Long healthId,

        String previousSterilizationStatus,

        String newSterilizationStatus,

        String previousVaccinationStatus,

        String newVaccinationStatus,

        String previousDisease,

        String newDisease,

        String action,

        LocalDateTime changedAt,

        String comment,

        Long changedByUserId

) {
}
