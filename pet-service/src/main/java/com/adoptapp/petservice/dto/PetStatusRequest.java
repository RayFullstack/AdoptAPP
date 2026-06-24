package com.adoptapp.petservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos para actualizar el estado de una mascota")
public record PetStatusRequest(
        String status
) {
}
