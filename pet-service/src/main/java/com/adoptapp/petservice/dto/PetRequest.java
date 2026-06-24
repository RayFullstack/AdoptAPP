package com.adoptapp.petservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.*;

@Schema(description = "Datos para crear o actualizar una mascota")
public record PetRequest (
        @NotBlank(message = "El nombre de la mascota es requerido")
        @Size(min = 2, max = 50)
        @Schema(description = "Nombre del registro", example = "Benito")
        String name,

        @NotBlank(message = "La especie es requerida")
        @Size(max = 50)
        @Schema(description = "Especie de la mascota", example = "Perro")
        String species,

        @NotBlank(message = "La raza es requerida")
        @Size(max = 50)
        @Schema(description = "Raza de la mascota", example = "Mestizo")
        String race,

        @NotNull(message = "La edad es requerida")
        @Min(value = 0, message = "La edad no puede ser negativa")
        @Schema(description = "Edad de la mascota", example = "3")
        Integer age,

        @NotBlank(message = "El tamaÃƒÂ±o es requerido")
        @Size(max = 50)
        @Schema(description = "Tamano de la mascota", example = "MEDIUM")
        String size,

        @NotBlank(message = "El color es requerido")
        @Size(max = 50)
        @Schema(description = "Color de la mascota", example = "Cafe")
        String color,

        @NotBlank(message = "La personalidad es requerida")
        @Size(max = 50)
        @Schema(description = "Personalidad de la mascota", example = "Tranquilo")
        String personality,

        @NotBlank(message = "El estado es requerido")
        @Size(max = 50)
        @Schema(description = "Estado actual del registro", example = "ACTIVE")
        String status,

        @NotNull(message = "El shelterId es requerido")
        Long shelterId
) {
}
