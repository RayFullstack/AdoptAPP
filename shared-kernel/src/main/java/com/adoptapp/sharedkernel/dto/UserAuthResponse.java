package com.adoptapp.sharedkernel.dto;

public record UserAuthResponse(
        Long id,
        String email,
        String password,
        String role,
        boolean enabled
) {
}
