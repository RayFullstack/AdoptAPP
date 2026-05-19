package com.adoptapp.supplyservice.dto;

public record UserAuthResponse(
        String email,
        String password,
        String role,
        boolean enabled
) {
}
