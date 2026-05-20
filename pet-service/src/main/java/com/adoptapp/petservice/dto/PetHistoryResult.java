package com.adoptapp.petservice.dto;

import java.time.LocalDateTime;

public record PetHistoryResult(
    Long id,
    Long petId,
    String previousName,
    String newName,
    String previousStatus,
    String newStatus,
    Long previousFosterId,
    Long newFosterId,
    Long changedByUserId,
    LocalDateTime changedAt,
    String comment
) {
}
