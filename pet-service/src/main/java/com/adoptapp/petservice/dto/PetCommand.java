package com.adoptapp.petservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos internos para procesar una mascota")
public record PetCommand (
        @Schema(description = "Nombre del registro", example = "Benito")
        String name,
        @Schema(description = "Especie de la mascota", example = "Perro")
        String species,
        @Schema(description = "Raza de la mascota", example = "Mestizo")
        String race,
        @Schema(description = "Edad de la mascota", example = "3")
        Integer age,
        @Schema(description = "Tamano de la mascota", example = "MEDIUM")
        String size,
        @Schema(description = "Color de la mascota", example = "Cafe")
        String color,
        @Schema(description = "Personalidad de la mascota", example = "Tranquilo")
        String personality,
        @Schema(description = "Estado actual del registro", example = "ACTIVE")
        String status,
        Long shelterId
){
}
