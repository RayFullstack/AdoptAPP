package com.adoptapp.notificationservice.dto;

import com.adoptapp.notificationservice.model.NotificationStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificationRequest(

        Long userId,

        Long shelterId,

        @NotBlank(message = "Recipient is required")
        String recipient,

        @NotBlank(message = "Message is required")
        String message,

        @NotBlank(message = "Type name is required")
        String typeName,

        @NotNull(message = "Status is required")
        NotificationStatus status
) {
}
