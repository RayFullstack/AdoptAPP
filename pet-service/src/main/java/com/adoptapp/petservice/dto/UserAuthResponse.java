package com.adoptapp.petservice.dto;

public record UserAuthResponse(
        String email,
        String password,
        UserRole role
) {
}
