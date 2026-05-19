package com.adoptapp.shelterservice.dto;

import com.adoptapp.shelterservice.model.ShelterStatus;

import java.time.LocalDateTime;

public record ShelterResponse(
        Long id,
        String name,
        String email,
        String phone,
        String description,
        ShelterStatus status,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
