package com.adoptapp.adoptionservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Información de una mascota consultada desde pet-service")
public record PetResponse(

        @Schema(description = "ID de la mascota", example = "7")
        Long id,

        @Schema(description = "nombre de la mascota", example = "Benito")
        String name,

        @Schema(description = "Especie de la mascota", example = "Perro")
        String species,

        @Schema(description = "Raza de la mascota", example = "Samoyedo")
        String race,

        @Schema(description = "Edad de la mascota", example = "7")
        int age,

        @Schema(description = "Tamaño de la mascota", example = "Pequeño")
        String size,

        @Schema(description = "Color de la mascota", example = "Gris")
        String color,

        @Schema(description = "Estado actual de la mascota", example = "AVAILABLE",
                allowableValues = {"AVAILABLE", "NOT_AVAILABLE", "DELETED"})
        String status,

        @Schema(description = "Personalidad de la mascota", example = "amoroso")
        String personality,

        @Schema(description = "ID del refugio en el que se encuentra la mascota", example = "2")
        Long shelterId
) {
}
