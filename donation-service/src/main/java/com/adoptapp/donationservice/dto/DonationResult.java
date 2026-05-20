package com.adoptapp.donationservice.dto;

import com.adoptapp.donationservice.model.DonationStatus;

<<<<<<< HEAD
=======
import java.math.BigDecimal;
import java.time.LocalDateTime;

>>>>>>> origin/camila-dev
public record DonationResult(

        Long id,
        String donorName,
<<<<<<< HEAD
        Double amount,
        String description,
        DonationStatus status
=======
        BigDecimal amount,
        String description,
        DonationStatus status,
        Long userId,
        Long shelterId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
>>>>>>> origin/camila-dev
) {
}