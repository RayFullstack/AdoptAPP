package com.adoptapp.adoptionservice.dto;

public record PetNotificationRequest(
        String title,
        String message,
        String type,
        String recipient
) {
}
