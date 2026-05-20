package com.adoptapp.donationservice.dto;

import com.adoptapp.donationservice.model.DonationStatus;
<<<<<<< HEAD

public record DonationResponse(

        Long id,
        String donorName,
        Double amount,
        String description,
        DonationStatus status
) {
}
=======
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DonationResponse(
        Long id,
        String donorName,
        BigDecimal amount,
        String description,
        DonationStatus status,
        Long userId,
        Long shelterId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
>>>>>>> origin/camila-dev
