package com.adoptapp.followupservice.dto;

public record UserAuthResponse(
        String email,
        String password,
        String role
) {
}
