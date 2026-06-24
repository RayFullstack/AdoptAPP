package com.adoptapp.petservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Informacion del refugio obtenida desde shelter-service")
public record ShelterResponse(
        @Schema(description = "ID del registro", example = "1")
        Long id,
        String email
) {
}
