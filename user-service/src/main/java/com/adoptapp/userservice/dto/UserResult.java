package com.adoptapp.userservice.dto;

import com.adoptapp.userservice.model.User;
import com.adoptapp.userservice.model.UserStatus;

public record UserResult(
        Long id,
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
        UserStatus status,

        User.Role role,

        boolean active
){
}
