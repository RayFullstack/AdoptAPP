package com.adoptapp.adoptionservice.dto;

import java.time.LocalDateTime;

public record AdoptionHistoryResponse(
        Long id,
        Long adoptionId,
        String action,
        String description,
        LocalDateTime createdAt
) {
}
