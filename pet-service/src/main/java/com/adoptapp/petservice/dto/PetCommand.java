package com.adoptapp.petservice.dto;

public record PetCommand (
        String name,
        String species,
        String race,
        Integer age,
        String size,
        String color,
        String personality,
        Long fosterId,
        boolean vaccinated,
        boolean sterilized,
        String diseases,
        String status
){
}
