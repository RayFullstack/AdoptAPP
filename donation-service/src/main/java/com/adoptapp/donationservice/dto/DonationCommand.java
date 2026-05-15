package com.adoptapp.donationservice.dto;

import com.adoptapp.donationservice.model.DonationStatus;

public record DonationCommand(

        String donorName,
        Double amount,
        String description,
        DonationStatus status
) {
}