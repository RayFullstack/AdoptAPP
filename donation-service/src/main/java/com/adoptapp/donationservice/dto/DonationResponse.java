package com.adoptapp.donationservice.dto;

import com.adoptapp.donationservice.model.DonationStatus;
import java.time.LocalDateTime;

public record DonationResponse(
        Long id,
        String donorName,
        Double amount,
        String description,
        DonationStatus status,
        Long userId,
        Long shelterId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
