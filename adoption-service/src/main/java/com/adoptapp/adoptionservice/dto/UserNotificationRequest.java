package com.adoptapp.adoptionservice.dto;

public record UserNotificationRequest (
        Long userId,
        String recipient,
        String message,
        String typeName,
        String status
){
}
