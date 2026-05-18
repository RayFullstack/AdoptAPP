package com.adoptapp.donationservice.dto;

import java.time.LocalDateTime;

public record DonationHistoryResponse(

        Long donationId,

        String action,

        String previousStatus,

        String newStatus,

        Double previousAmount,

        Double newAmount,

        String comment,

        LocalDateTime changedAt,

        Long changedByUserId
) {
}
