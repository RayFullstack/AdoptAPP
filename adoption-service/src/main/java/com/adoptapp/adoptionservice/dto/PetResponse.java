package com.adoptapp.adoptionservice.dto;

public record PetResponse(
        Long id,
        String name,
        String species,
        String race,
        int age,
        String size,
        String color,
        String status,
        Boolean vaccinated,
        Boolean sterilized,
        String diseases,
        String personality,
        Long fosterId,
        Long shelterId
) {
}
