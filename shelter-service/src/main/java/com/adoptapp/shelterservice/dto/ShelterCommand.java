package com.adoptapp.shelterservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import com.adoptapp.shelterservice.model.ShelterStatus;

@Schema(description = "Datos internos para procesar un refugio")
public record ShelterCommand(
        @Schema(description = "Nombre del registro", example = "Benito")
        String name,
        @Schema(description = "Correo electronico", example = "usuario@mail.com")
        String email,
        @Schema(description = "Numero de telefono", example = "912345678")
        String phone,
        @Schema(description = "Descripcion del registro", example = "Descripcion del registro")
        String description,
        ShelterStatus status
) {
}
