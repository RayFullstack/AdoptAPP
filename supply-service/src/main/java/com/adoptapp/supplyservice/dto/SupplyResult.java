package com.adoptapp.supplyservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Resultado interno de un insumo")
public record SupplyResult(
        @Schema(description = "ID del registro", example = "1")
        Long id,
        @Schema(description = "Nombre del registro", example = "Benito")
        String name,
        @Schema(description = "Descripcion del registro", example = "Descripcion del registro")
        String description,
        @Schema(description = "Cantidad disponible", example = "15")
        Integer quantity,
        @Schema(description = "Unidad de medida", example = "KG")
        String unit,
        @Schema(description = "Categoria del registro", example = "FOOD")
        String category,
        @Schema(description = "ID del refugio asociado", example = "2")
        Long shelterId,
        @Schema(description = "Nombre del proveedor", example = "Proveedor Animal")
        String supplierName,
        @Schema(description = "Stock minimo recomendado", example = "5")
        Integer minimumStock,
        @Schema(description = "Estado actual del registro", example = "ACTIVE")
        String status,
        @Schema(description = "Fecha y hora de creacion del registro", example = "2026-04-21T14:30:00")
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
