package com.adoptapp.shelterservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import com.adoptapp.shelterservice.model.ShelterStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Datos para crear o actualizar un refugio")
public record ShelterRequest(

        @NotBlank(message = "Name is required")
        @Schema(description = "Nombre del registro", example = "Benito")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Schema(description = "Correo electronico", example = "usuario@mail.com")
        String email,

        @Schema(description = "Numero de telefono", example = "912345678")
        String phone,

        @Schema(description = "Descripcion del registro", example = "Descripcion del registro")
        String description,

        ShelterStatus status
) {
}
