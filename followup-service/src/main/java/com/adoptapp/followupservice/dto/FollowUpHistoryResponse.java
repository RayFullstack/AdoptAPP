package com.adoptapp.followupservice.dto;

import java.time.LocalDateTime;

public record FollowUpHistoryResponse(
        Long followUpId,
        String action,
        String previousStatus,
        String newStatus,
        String comment,
        Long changedByUserId,
        LocalDateTime changedAt
) {
}
