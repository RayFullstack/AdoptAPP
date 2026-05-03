package com.adoptapp.petservice.dto;

import jakarta.validation.constraints.*;

public record PetRequest (
        @NotBlank(message = "El nombre de la mascota es requerido")
        @Size(min = 2, max = 50)
        String name,

        @NotBlank(message = "La especie es requerida")
        @Size(max = 50)
        String species,

        @NotBlank(message = "La raza es requerida")
        @Size(max = 50)
        String race,

        @NotNull(message = "La edad es requerida")
        @Min(value = 0, message = "La edad no puede ser negativa")
        Integer age,

        @NotBlank(message = "El tamaño es requerido")
        @Size(max = 50)
        String size,

        @NotBlank(message = "El color es requerido")
        @Size(max = 50)
        String color,

        @NotBlank(message = "El estado de salud es requerido")
        String health,

        @NotBlank(message = "La personalidad es requerida")
        String personality,

        @NotNull(message = "El fosterId es requerido")
        Long fosterId,

        String status
) {
}
