package com.adoptapp.userservice.dto;

public record UserNotificationRequest(
        Long userId,
        String recipient,
        String message,
        String typeName,
        String status
) {
}
