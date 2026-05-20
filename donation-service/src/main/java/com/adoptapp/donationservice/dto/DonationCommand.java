package com.adoptapp.donationservice.dto;

import com.adoptapp.donationservice.model.DonationStatus;

<<<<<<< HEAD
public record DonationCommand(

        String donorName,
        Double amount,
        String description,
        DonationStatus status
=======
import java.math.BigDecimal;

public record DonationCommand(

        String donorName,
        BigDecimal amount,
        String description,
        DonationStatus status,
        Long userId,
        Long shelterId
>>>>>>> origin/camila-dev
) {
}