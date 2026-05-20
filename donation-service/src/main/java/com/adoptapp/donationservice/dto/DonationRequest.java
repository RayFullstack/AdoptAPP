package com.adoptapp.donationservice.dto;

import com.adoptapp.donationservice.model.DonationStatus;
import jakarta.validation.constraints.NotBlank;
<<<<<<< HEAD
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
=======
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
>>>>>>> origin/camila-dev

public record DonationRequest(

        @NotBlank(message = "Donor name is required")
        String donorName,

        @NotNull(message = "Amount is required")
<<<<<<< HEAD
        @Positive(message = "Amount must be greater than 0")
        Double amount,
=======
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        BigDecimal amount,
>>>>>>> origin/camila-dev

        @NotBlank(message = "Description is required")
        String description,

<<<<<<< HEAD
        DonationStatus status
=======
        DonationStatus status,

        @NotNull(message = "User ID is required")
        Long userId,

        @NotNull(message = "Shelter ID is required")
        Long shelterId
>>>>>>> origin/camila-dev
) {
}