package com.adoptapp.shelterservice.dto;

import java.time.LocalDateTime;

public record ShelterHistoryResponse(

        Long shelterId,

        String action,

        String previousName,

        String newName,

        String previousEmail,

        String newEmail,

        String previousPhone,

        String newPhone,

        String previousDescription,

        String newDescription,

        String previousStatus,

        String newStatus,

        Boolean previousActive,

        Boolean newActive,

        String comment,

        Long changedByUserId,

        LocalDateTime changedAt
) {
}
