package com.adoptapp.petservice.dto;

public record NotificationRequest(
        Long userId,
        String recipient,
        String message,
        String typeName,
        String status
) {
}
