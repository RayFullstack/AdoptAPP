package com.adoptapp.petservice.dto;

public record PetResponse(
        Long id,
        String name,
        String species,
        String race,
        int age,
        String size,
        String color,
        String status,
        String personality,
        Long shelterId)
{
}
