package com.adoptapp.supplyservice.dto;

import java.time.LocalDateTime;

public record SupplyHistoryResponse(
        Long id,
        Long supplyId,
        String action,
        String comment,
        String prevStatus,
        String newStatus,
        Integer prevQuantity,
        Integer newQuantity,
        String prevCategory,
        String newCategory,
        Long changedByUserId,
        LocalDateTime createdAt
) {
}
