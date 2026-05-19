package com.adoptapp.donationservice.dto;

import com.adoptapp.donationservice.model.DonationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DonationRequest(

        @NotBlank(message = "Donor name is required")
        String donorName,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than 0")
        Double amount,

        @NotBlank(message = "Description is required")
        String description,

        DonationStatus status,

        @NotNull(message = "User ID is required")
        Long userId,

        @NotNull(message = "Shelter ID is required")
        Long shelterId
) {
}