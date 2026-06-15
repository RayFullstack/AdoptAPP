package com.adoptapp.adoptionservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record AdoptionCreateRequest(
        @Schema(description = "ID de la mascota que se desea adoptar", example = "1")
        @NotNull(message = "El petId es requerido")
        Long petId,

        @Schema(description = "ID del usuario adoptante. SOLO ADMIN o SHELTER_ADMIN " +
                "pueden enviarlo; si el rol es ADOPTER, se usa automaticamente el usuario autenticado",
                example= "1" )
        Long userId

){
}
