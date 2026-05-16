package com.adoptapp.adoptionservice.dto;

import java.time.LocalDateTime;

public record AdoptionResult(
        Long id,
        Long userId,
        Long petId,
        String status,
        LocalDateTime createdAt
) {
}