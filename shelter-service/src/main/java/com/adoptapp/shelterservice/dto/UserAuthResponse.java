package com.adoptapp.shelterservice.dto;

public record UserAuthResponse(
        String email,
        String password,
        String role
) {
}
