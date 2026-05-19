package com.adoptapp.staffservice.dto;

public record UserAuthResponse(
        String email,
        String password,
        String role
) {
}
