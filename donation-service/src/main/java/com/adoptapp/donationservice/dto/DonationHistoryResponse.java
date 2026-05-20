package com.adoptapp.donationservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DonationHistoryResponse(

        Long donationId,

        String action,

        String previousStatus,

        String newStatus,

        BigDecimal previousAmount,

        BigDecimal newAmount,

        String comment,

        LocalDateTime changedAt,

        Long changedByUserId
) {
}
