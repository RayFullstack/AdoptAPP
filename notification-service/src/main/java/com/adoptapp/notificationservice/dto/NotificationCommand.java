package com.adoptapp.notificationservice.dto;

import com.adoptapp.notificationservice.model.NotificationStatus;

public record NotificationCommand(

        Long userId,
        String recipient,
        String message,
        String typeName,
        NotificationStatus status
) {
}
