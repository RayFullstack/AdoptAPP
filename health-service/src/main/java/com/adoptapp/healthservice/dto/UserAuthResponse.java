package com.adoptapp.healthservice.dto;

public record UserAuthResponse(
        String email,
        String password,
        UserRole role
) {
}
