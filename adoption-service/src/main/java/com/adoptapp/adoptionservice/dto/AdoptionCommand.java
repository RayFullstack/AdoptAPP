package com.adoptapp.adoptionservice.dto;

public record AdoptionCommand (
        Long userId,
        Long petId,
        String status
){
}