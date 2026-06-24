package com.adoptapp.supplyservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Informacion de un cambio registrado en el historial de un insumo")
public record SupplyHistoryResponse(
        @Schema(description = "ID del registro", example = "1")
        Long id,
        @Schema(description = "ID del insumo asociado", example = "6")
        Long supplyId,
        @Schema(description = "Accion registrada en el historial", example = "UPDATED")
        String action,
        @Schema(description = "Comentario del historial", example = "Registro actualizado")
        String comment,
        @Schema(description = "Estado anterior del insumo", example = "ACTIVE")
        String prevStatus,
        @Schema(description = "Estado nuevo del registro", example = "APPROVED")
        String newStatus,
        @Schema(description = "Cantidad anterior del insumo", example = "10")
        Integer prevQuantity,
        @Schema(description = "Cantidad nueva del insumo", example = "15")
        Integer newQuantity,
        @Schema(description = "Categoria anterior del insumo", example = "FOOD")
        String prevCategory,
        @Schema(description = "Categoria nueva del insumo", example = "MEDICINE")
        String newCategory,
        @Schema(description = "ID del usuario que realizo el cambio", example = "1")
        Long changedByUserId,
        LocalDateTime createdAt
) {
}
