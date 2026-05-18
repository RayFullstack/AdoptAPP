package com.adoptapp.donationservice.dto;

public record NotificationRequest(
        Long userId,
        String recipient,
        String message,
        String typeName,
        String status
) {
}
