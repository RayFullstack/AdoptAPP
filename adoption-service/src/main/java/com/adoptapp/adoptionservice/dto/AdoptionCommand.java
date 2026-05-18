package com.adoptapp.adoptionservice.dto;

import com.adoptapp.adoptionservice.model.AdoptionStatus;

public record AdoptionCommand (
        Long userId,
        Long petId,
        AdoptionStatus status
){
}