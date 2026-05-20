package com.adoptapp.notificationservice.dto;

import com.adoptapp.notificationservice.model.NotificationStatus;

public record NotificationCommand(

<<<<<<< HEAD
        String recipient,
        String message,
        String type,
        NotificationStatus status
) {
}
=======
        Long userId,
        String recipient,
        String message,
        String typeName,
        NotificationStatus status
) {
}
>>>>>>> origin/camila-dev
