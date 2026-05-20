package com.adoptapp.adoptionservice.dto;

public record AdoptionResponse(

        Long id,
        Long userId,
        Long petId,
        String status
) {
}