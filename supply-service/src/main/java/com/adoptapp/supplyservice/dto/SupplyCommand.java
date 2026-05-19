package com.adoptapp.supplyservice.dto;

import com.adoptapp.supplyservice.model.SupplyStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SupplyCommand(
        @NotBlank(message = "El nombre es obligatorio")
        String name,

        String description,

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 0, message = "La cantidad no puede ser negativa")
        Integer quantity,

        @NotBlank(message = "La unidad es obligatoria")
        String unit,

        @NotBlank(message = "La categoría es obligatoria")
        String category,

        @NotNull(message = "El shelterId es obligatorio")
        Long shelterId,

        @NotNull(message = "El userId es obligatorio")
        Long userId,

        String supplierName,

        @Min(value = 0, message = "El stock mínimo no puede ser negativo")
        Integer minimumStock,

        @NotNull(message = "El estado es obligatorio")
        SupplyStatus status
) {
}
