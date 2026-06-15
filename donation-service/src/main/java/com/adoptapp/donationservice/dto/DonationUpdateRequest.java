package com.adoptapp.donationservice.dto;

import com.adoptapp.donationservice.model.DonationStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DonationUpdateRequest(
        @NotBlank(message = "Donor name is required")
        String donorName,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        BigDecimal amount,

        @NotBlank(message = "Description is required")
        String description,

        DonationStatus status,

        @NotNull(message = "User ID is required")
        Long userId,

        @NotNull(message = "Shelter ID is required")
        Long shelterId
) {
}