package com.adoptapp.staffservice.dto;

public record NotificationRequest(
        Long userId,
        String recipient,
        String message,
        String typeName,
        String status
) {
}
