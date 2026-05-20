package com.adoptapp.adoptionservice.dto;

<<<<<<< HEAD
import jakarta.validation.constraints.NotNull;

public record AdoptionRequest(

        @NotNull
        Long userId,

        @NotNull
        Long petId,

        @NotNull
        String status
) {
}
=======
import com.adoptapp.adoptionservice.model.AdoptionStatus;
import jakarta.validation.constraints.*;

public record AdoptionRequest(
        @NotNull(message = "El petId es requerido")
        Long petId,

        @NotNull(message = "El userId es requerido")
        Long userId,

        @NotNull(message = "El estado es requerido")
        AdoptionStatus status
){
}
>>>>>>> origin/camila-dev
