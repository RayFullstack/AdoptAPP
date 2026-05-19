package com.adoptapp.staffservice.dto;

import com.adoptapp.staffservice.model.StaffPosition;
import com.adoptapp.staffservice.model.StaffStatus;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record StaffRequest(

        @NotNull(message = "User ID is required")
        Long userId,

        @NotNull(message = "Shelter ID is required")
        Long shelterId,

        @NotNull(message = "Position is required")
        StaffPosition position,

        String phone,

        @Email(message = "Email must be valid")
        String email,

        LocalDateTime hireDate,

        StaffStatus status
) {
}
