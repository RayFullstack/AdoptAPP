package com.adoptapp.adoptionservice.dto;

<<<<<<< HEAD
public record AdoptionCommand(

        Long userId,
        Long petId,
        String status
) {
=======
import com.adoptapp.adoptionservice.model.AdoptionStatus;

public record AdoptionCommand (
        Long userId,
        Long petId,
        AdoptionStatus status
){
>>>>>>> origin/camila-dev
}