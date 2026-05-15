package com.adoptapp.adoptionservice.dto;

public record AdoptionResult(
        Long id,
        String petName,
        String adopterName,
        String status
        // Si aquí tienes un quinto campo, el Service fallará.
) {
}