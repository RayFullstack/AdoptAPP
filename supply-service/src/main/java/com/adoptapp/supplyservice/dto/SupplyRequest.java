package com.adoptapp.supplyservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import com.adoptapp.supplyservice.model.SupplyStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Datos para crear o actualizar un insumo")
public record SupplyRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Schema(description = "Nombre del registro", example = "Benito")
        String name,

        @Schema(description = "Descripcion del registro", example = "Descripcion del registro")
        String description,

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 0, message = "La cantidad no puede ser negativa")
        @Schema(description = "Cantidad disponible", example = "15")
        Integer quantity,

        @NotBlank(message = "La unidad es obligatoria")
        @Schema(description = "Unidad de medida", example = "KG")
        String unit,

        @NotBlank(message = "La categorÃƒÂ­a es obligatoria")
        @Schema(description = "Categoria del registro", example = "FOOD")
        String category,

        @NotNull(message = "El shelterId es obligatorio")
        @Schema(description = "ID del refugio asociado", example = "2")
        Long shelterId,

        @NotNull(message = "El userId es obligatorio")
        @Schema(description = "ID del usuario asociado", example = "1")
        Long userId,

        @Schema(description = "Nombre del proveedor", example = "Proveedor Animal")
        String supplierName,

        @Min(value = 0, message = "El stock mÃƒÂ­nimo no puede ser negativo")
        @Schema(description = "Stock minimo recomendado", example = "5")
        Integer minimumStock,

        @NotNull(message = "El estado es obligatorio")
        SupplyStatus status
) {
}
