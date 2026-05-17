package com.adoptapp.notificationservice.dto;

import com.adoptapp.notificationservice.model.NotificationStatus;

import java.time.LocalDateTime;

public record NotificationResult(

        Long id,
        Long userId,
        String recipient,
        String message,
        Long typeId,
        String typeName,
        NotificationStatus status,
        LocalDateTime createdAt
) {
}
