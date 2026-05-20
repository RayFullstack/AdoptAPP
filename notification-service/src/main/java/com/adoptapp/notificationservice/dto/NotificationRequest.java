package com.adoptapp.notificationservice.dto;

import com.adoptapp.notificationservice.model.NotificationStatus;
<<<<<<< HEAD
=======

>>>>>>> origin/camila-dev
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificationRequest(

<<<<<<< HEAD
=======
        Long userId,

>>>>>>> origin/camila-dev
        @NotBlank(message = "Recipient is required")
        String recipient,

        @NotBlank(message = "Message is required")
        String message,

<<<<<<< HEAD
        @NotBlank(message = "Type is required")
        String type,
=======
        @NotBlank(message = "Type name is required")
        String typeName,
>>>>>>> origin/camila-dev

        @NotNull(message = "Status is required")
        NotificationStatus status
) {
<<<<<<< HEAD
}
=======
}
>>>>>>> origin/camila-dev
