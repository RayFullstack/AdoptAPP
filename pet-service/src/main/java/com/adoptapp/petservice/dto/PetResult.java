package com.adoptapp.petservice.dto;

public record PetResult(
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
        Long fosterId
) {
}
