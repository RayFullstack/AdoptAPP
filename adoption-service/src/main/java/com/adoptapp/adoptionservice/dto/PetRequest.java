package com.adoptapp.adoptionservice.dto;

public record PetRequest(
        String action,
        String entityType,
        Long entityId,
        Long petId,
        String petName,
        String details
){
}
