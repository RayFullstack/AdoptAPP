package com.adoptapp.adoptionservice.dto;

public record AdoptionResult(

        Long id,
        Long userId,
        Long petId,
        String status
) {
}