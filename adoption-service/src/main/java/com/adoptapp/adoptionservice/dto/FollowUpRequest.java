package com.adoptapp.adoptionservice.dto;

import java.time.LocalDateTime;

public record FollowUpRequest(
        String adopterName,
        String petName,
        Long userId,
        Long petId,
        Long adoptionId,
        LocalDateTime visitDate,
        String comments,
        String status
) {
}
