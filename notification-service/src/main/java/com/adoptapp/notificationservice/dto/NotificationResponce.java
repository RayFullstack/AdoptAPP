package com.adoptapp.notificationservice.dto;

import com.adoptapp.notificationservice.model.NotificationStatus;

public record NotificationResponse(

        Long id,
        String recipient,
        String message,
        String type,
        NotificationStatus status
) {
}