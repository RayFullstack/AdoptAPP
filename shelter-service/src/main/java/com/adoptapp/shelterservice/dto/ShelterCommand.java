package com.adoptapp.shelterservice.dto;

import com.adoptapp.shelterservice.model.ShelterStatus;

public record ShelterCommand(
        String name,
        String email,
        String phone,
        String description,
        ShelterStatus status
) {
}
