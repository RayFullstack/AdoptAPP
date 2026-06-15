package com.adoptapp.notificationservice.dto;

import com.adoptapp.notificationservice.model.NotificationStatus;

import java.time.LocalDateTime;

public record NotificationResponse(

        Long id,
        Long userId,
        Long shelterId,
        String recipient,
        String message,
        Long typeId,
        String typeName,
        NotificationStatus status,
        LocalDateTime createdAt
) {
}
