package com.adoptapp.adoptionservice.dto;

import java.time.LocalDateTime;

public record AdoptionResponse(
        Long id,
        Long userId,
        Long petId,
        String status,
        LocalDateTime createdAt) {
                }
