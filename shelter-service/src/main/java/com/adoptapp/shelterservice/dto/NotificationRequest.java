package com.adoptapp.shelterservice.dto;

public record NotificationRequest (
        Long userId,
        Long shelterId,
        String recipient,
        String message,
        String typeName,
        String status
){

}
