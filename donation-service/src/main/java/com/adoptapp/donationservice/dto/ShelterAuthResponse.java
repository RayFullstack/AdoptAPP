package com.adoptapp.donationservice.dto;

public record ShelterAuthResponse(
        String email,
        String password,
        UserRole role
) {
}
