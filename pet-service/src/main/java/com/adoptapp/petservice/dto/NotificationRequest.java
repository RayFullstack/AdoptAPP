package com.adoptapp.petservice.dto;

public record NotificationRequest(
        Long userId,
        Long shelterId,
        String recipient,
        String message,
        String typeName,
        String status
) {
}
