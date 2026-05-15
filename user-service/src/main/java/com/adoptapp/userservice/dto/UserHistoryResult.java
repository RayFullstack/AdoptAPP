package com.adoptapp.userservice.dto;

import java.time.LocalDateTime;

public record UserHistoryResult(
        Long id,
        Long userId,
        String previousName,
        String newName,
        String previousSurname,
        String newSurname,
        String previousUsername,
        String newUsername,
        String previousEmail,
        String newEmail,
        String previousPhone,
        String newPhone,
        String previousStatus,
        String newStatus,
        String previousRole,
        String newRole,
        Boolean previousActive,
        Boolean newActive,
        LocalDateTime changedAt,
        String comment
) {
}
