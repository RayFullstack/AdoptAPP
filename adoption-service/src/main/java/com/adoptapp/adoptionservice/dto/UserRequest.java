package com.adoptapp.adoptionservice.dto;

public record UserRequest(
        String action,
        String entityType,
        Long entityId,
        Long userId,
        String username,
        String details
){

}
