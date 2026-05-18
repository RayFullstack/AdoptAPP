package com.adoptapp.healthservice.dto;

public record NotificationRequest(
        Long userId,
        String recipient,
        String message,
        String typeName,
        String status
) {
}
