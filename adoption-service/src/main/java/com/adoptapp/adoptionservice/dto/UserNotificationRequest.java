package com.adoptapp.adoptionservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserNotificationRequest (
        @Schema(description = "ID del usuario adoptante",
                example = "1")
        Long userId,

        @Schema(description = "nombre del usuario que recibe la",
                example = "1")
        String recipient,
        String message,
        String typeName,
        String status
){
}
