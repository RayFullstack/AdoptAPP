package com.adoptapp.adoptionservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Información del usuario obtenida desde user-service")
public record UserResponse(
        @Schema(description = "ID del usuario", example = "55")
        Long id,

        @Schema(description = "Email del usuario", example = "adopter@mail.com")
        String email
) {
}
