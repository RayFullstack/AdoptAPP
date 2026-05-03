package com.adoptapp.petservice.dto;

public record PetResult(
        Long id,
        String name,
        String species,
        String race,
        int age,
        String size,
        String color,
        String health,
        String personality,
        Long fosterId
) {
}
