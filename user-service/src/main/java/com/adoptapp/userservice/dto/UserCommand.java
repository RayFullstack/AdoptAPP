package com.adoptapp.userservice.dto;

import com.adoptapp.userservice.model.UserStatus;

import java.time.LocalDateTime;

public record UserCommand(
        String username,
        String name,
        String surname,
        String email,

        String phone,

        String country,
        String city,
        String street,
        String homeNumber,
        String postalCode,
        String type,

        UserStatus status
) {
}
