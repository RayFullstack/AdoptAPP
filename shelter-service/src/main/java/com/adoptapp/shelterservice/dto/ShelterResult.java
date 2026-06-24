package com.adoptapp.shelterservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import com.adoptapp.shelterservice.model.ShelterStatus;

import java.time.LocalDateTime;

@Schema(description = "Resultado interno de un refugio")
public record ShelterResult(
        @Schema(description = "ID del registro", example = "1")
        Long id,
        @Schema(description = "Nombre del registro", example = "Benito")
        String name,
        @Schema(description = "Correo electronico", example = "usuario@mail.com")
        String email,
        @Schema(description = "Numero de telefono", example = "912345678")
        String phone,
        @Schema(description = "Descripcion del registro", example = "Descripcion del registro")
        String description,
        @Schema(description = "Estado actual del registro", example = "ACTIVE")
        ShelterStatus status,
        @Schema(description = "Indica si el registro esta activo", example = "true")
        boolean active,
        @Schema(description = "Fecha y hora de creacion del registro", example = "2026-04-21T14:30:00")
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
