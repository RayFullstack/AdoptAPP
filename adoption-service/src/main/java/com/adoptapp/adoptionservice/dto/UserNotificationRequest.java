package com.adoptapp.adoptionservice.dto;

public record UserNotificationRequest (
        String title,
        String message,
        String type,
        String recipient
){
}
