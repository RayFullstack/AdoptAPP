package com.adoptapp.adoptionservice.dto;

import java.time.LocalDateTime;

public record AdoptionResult(
        Long id,
        Long petId,
        Long userId,
        String status,
        LocalDateTime createdAt) {
}
