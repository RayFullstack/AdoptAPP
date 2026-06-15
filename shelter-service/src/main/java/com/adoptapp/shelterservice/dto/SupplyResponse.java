package com.adoptapp.shelterservice.dto;

import java.time.LocalDateTime;

public record SupplyResponse(
        Long id,
        String name,
        String description,
        Integer quantity,
        String unit,
        String category,
        Long shelterId,
        String supplierName,
        Integer minimumStock,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
