package com.adoptapp.userservice.dto;

import java.time.LocalDateTime;

public record UserCommand(
        String username,
        String name,
        String surname,
        String email,
        String phone,
        String address,
        String status) {
}
