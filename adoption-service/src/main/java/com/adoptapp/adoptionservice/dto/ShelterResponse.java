package com.adoptapp.adoptionservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Información de una refugio otenida desde shelter-service")
public record ShelterResponse(
        @Schema(description = "ID de un refugio", example = "67")
        Long id,

        @Schema(description = "Email de un refugio", example = "refugio@mail.com ")
        String email
) {
}
