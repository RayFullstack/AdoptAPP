package com.adoptapp.adoptionservice.dto;

import jakarta.validation.constraints.*;


import java.time.LocalDateTime;

public record AdoptionRequest(
        @NotNull(message = "El petId es requerido")
        Long petId,

        @NotNull(message = "El userId es requerido")
        Long userId,

        String status
){
}
