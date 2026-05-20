package com.adoptapp.adoptionservice.dto;

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