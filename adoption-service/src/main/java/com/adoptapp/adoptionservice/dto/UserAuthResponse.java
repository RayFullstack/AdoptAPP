package com.adoptapp.adoptionservice.dto;

public record UserAuthResponse(
        String email,
        String password,
        UserRole role
) {
}
