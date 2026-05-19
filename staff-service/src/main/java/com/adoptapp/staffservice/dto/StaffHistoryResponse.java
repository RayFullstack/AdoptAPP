package com.adoptapp.staffservice.dto;

import java.time.LocalDateTime;

public record StaffHistoryResponse(
        Long staffId,
        String action,
        String previousPosition,
        String newPosition,
        String previousStatus,
        String newStatus,
        String previousPhone,
        String newPhone,
        String comment,
        Long changedByUserId,
        LocalDateTime changedAt
) {
}
