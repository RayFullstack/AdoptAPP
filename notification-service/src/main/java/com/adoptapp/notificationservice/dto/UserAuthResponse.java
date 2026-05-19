package com.adoptapp.notificationservice.dto;

public record UserAuthResponse(
        String email,
        String password,
        String role,
        boolean enabled
) {
}
