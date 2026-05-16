package com.adoptapp.adoptionservice.dto;

import java.time.LocalDateTime;

public record PetEvent (
        Long id,
        String action,
        String entityType,
        Long entityId,
        Long petId,
        String petName,
        String details,
        LocalDateTime timestamp
){
}
