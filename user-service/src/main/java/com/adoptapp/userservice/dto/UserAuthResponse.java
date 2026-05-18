package com.adoptapp.userservice.dto;

import com.adoptapp.userservice.model.User;

public record UserAuthResponse(
        String email,
        String password,
        User.Role role
) {
}
