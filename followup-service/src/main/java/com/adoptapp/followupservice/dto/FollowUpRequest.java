package com.adoptapp.followupservice.dto;

import com.adoptapp.followupservice.model.FollowUpStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record FollowUpRequest(

        @NotBlank(message = "Adopter name is required")
        String adopterName,

        @NotBlank(message = "Pet name is required")
        String petName,

        Long userId,

        Long petId,

        Long adoptionId,

        @NotNull(message = "Visit date is required")
        LocalDateTime visitDate,

        String comments,

        @NotNull(message = "Status is required")
        FollowUpStatus status
) {
}
