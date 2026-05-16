package com.adoptapp.followupservice.dto;

import com.adoptapp.followupservice.model.FollowUpStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FollowUpRequest(

        @NotBlank(message = "Adopter name is required")
        String adopterName,

        @NotBlank(message = "Pet name is required")
        String petName,

        @NotBlank(message = "Visit date is required")
        String visitDate,

        @NotBlank(message = "Comments are required")
        String comments,

        @NotNull(message = "Status is required")
        FollowUpStatus status
) {
}