package com.adoptapp.adoptionservice.dto;

import java.time.LocalDateTime;

public record AdoptionCommand (
        Long id,
        Long petId,
        String petName,      // Añadido
        Long userId,
        String adopterName,  // Añadido
        String status,
        LocalDateTime createdAt
){
}