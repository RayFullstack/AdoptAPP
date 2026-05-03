package com.adoptapp.userservice.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserResult(
        Long id,
        String username,
        String name,
        String surname,
        String email,
        String phone,
        String address,
        String status,
        LocalDateTime createdAt){
}
