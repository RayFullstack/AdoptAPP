package com.adoptapp.notificationservice.dto;

import com.adoptapp.notificationservice.model.NotificationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificationRequest(

        @NotBlank(message = "Recipient is required")
        String recipient,

        @NotBlank(message = "Message is required")
        String message,

        @NotBlank(message = "Type is required")
        String type,

        @NotNull(message = "Status is required")
        NotificationStatus status
) {
}