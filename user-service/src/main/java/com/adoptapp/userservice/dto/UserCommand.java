package com.adoptapp.userservice.dto;

import com.adoptapp.userservice.model.User;
import com.adoptapp.userservice.model.UserStatus;

public record UserCommand(
        String username,
        String name,
        String surname,
        String email,

        String password,

        String phone,

        String country,
        String city,
        String street,
        String homeNumber,
        String postalCode,
        String type,

        UserStatus status,

        User.Role role,

        boolean active
) {
}
