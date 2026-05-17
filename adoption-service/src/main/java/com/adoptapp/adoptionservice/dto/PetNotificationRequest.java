package com.adoptapp.adoptionservice.dto;

public record PetNotificationRequest(
        Long userId,
        String recipient,
        String message,
        String typeName,
        String status
) {
}
