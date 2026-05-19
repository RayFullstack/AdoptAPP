package com.adoptapp.donationservice.dto;

import com.adoptapp.donationservice.model.DonationStatus;

import java.math.BigDecimal;

public record DonationCommand(

        String donorName,
        BigDecimal amount,
        String description,
        DonationStatus status,
        Long userId,
        Long shelterId
) {
}