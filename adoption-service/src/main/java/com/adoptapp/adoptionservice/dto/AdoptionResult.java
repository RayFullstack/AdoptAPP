package com.adoptapp.adoptionservice.dto;

import com.adoptapp.adoptionservice.model.AdoptionStatus;
import java.time.LocalDateTime;

public record AdoptionResult(
        Long id,
        Long userId,
        Long petId,
        AdoptionStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}