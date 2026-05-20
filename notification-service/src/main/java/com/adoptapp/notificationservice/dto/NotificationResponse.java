package com.adoptapp.notificationservice.dto;

import com.adoptapp.notificationservice.model.NotificationStatus;

<<<<<<< HEAD
public record NotificationResponse(

        Long id,
        String recipient,
        String message,
        String type,
        NotificationStatus status
) {
}
=======
import java.time.LocalDateTime;

public record NotificationResponse(

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
>>>>>>> origin/camila-dev
