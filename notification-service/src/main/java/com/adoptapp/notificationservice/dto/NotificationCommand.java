package com.adoptapp.notificationservice.dto;

import com.adoptapp.notificationservice.model.NotificationStatus;

public record NotificationCommand(

        String recipient,
        String message,
        String type,
        NotificationStatus status
) {
}