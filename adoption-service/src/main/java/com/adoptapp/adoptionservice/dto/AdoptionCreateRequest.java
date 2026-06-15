package com.adoptapp.adoptionservice.dto;

import com.adoptapp.adoptionservice.model.AdoptionStatus;
import jakarta.validation.constraints.*;

public record AdoptionRequest(
        @NotNull(message = "El petId es requerido")
        Long petId,

        @NotNull(message = "El userId es requerido")
        Long userId

){
}
