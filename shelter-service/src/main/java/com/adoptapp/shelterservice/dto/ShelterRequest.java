package com.adoptapp.shelterservice.dto;

import com.adoptapp.shelterservice.model.ShelterStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ShelterRequest(

        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        String phone,

        String description,

        ShelterStatus status
) {
}
