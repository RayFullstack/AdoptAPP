package com.adoptapp.petservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Informacion de la mascota obtenida desde pet-service")
public record PetResponse(
        @Schema(description = "ID del registro", example = "1")
        Long id,
        @Schema(description = "Nombre del registro", example = "Benito")
        String name,
        @Schema(description = "Especie de la mascota", example = "Perro")
        String species,
        @Schema(description = "Raza de la mascota", example = "Mestizo")
        String race,
        @Schema(description = "Edad de la mascota", example = "3")
        int age,
        @Schema(description = "Tamano de la mascota", example = "MEDIUM")
        String size,
        @Schema(description = "Color de la mascota", example = "Cafe")
        String color,
        @Schema(description = "Estado actual del registro", example = "ACTIVE")
        String status,
        @Schema(description = "Personalidad de la mascota", example = "Tranquilo")
        String personality,
        @Schema(description = "ID del refugio asociado", example = "2")
        Long shelterId)
{
}
