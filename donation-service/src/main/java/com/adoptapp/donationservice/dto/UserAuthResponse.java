package com.adoptapp.donationservice.dto;

public record UserAuthResponse(
        String email,
        String password,
        UserRole role
) {
}
