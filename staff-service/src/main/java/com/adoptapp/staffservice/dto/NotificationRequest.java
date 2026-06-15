package com.adoptapp.staffservice.dto;

public record NotificationRequest(
        Long userId,
        Long shelterId,
        String recipient,
        String message,
        String typeName,
        String status
) {
}
