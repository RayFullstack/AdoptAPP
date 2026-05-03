package com.adoptapp.petservice.dto;

public record PetCommand (
        String name,
        String species,
        String race,
        Integer age,
        String size,
        String color,
        String health,
        String personality,
        Long fosterId,
        String status
){
}
