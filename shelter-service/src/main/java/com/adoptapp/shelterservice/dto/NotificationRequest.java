package com.adoptapp.shelterservice.dto;

public record NotificationRequest (
        Long userId,
        String recipient,
        String message,
        String typeName,
        String status
){

}
