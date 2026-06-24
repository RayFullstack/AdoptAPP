package com.adoptapp.staffservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Informacion del usuario obtenida desde user-service")
public record UserResponse(
        @Schema(description = "ID del registro", example = "1")
        Long id,
        String email
) {
}
