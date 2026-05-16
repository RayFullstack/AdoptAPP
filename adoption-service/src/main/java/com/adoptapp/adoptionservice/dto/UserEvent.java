package com.adoptapp.adoptionservice.dto;

import java.time.LocalDateTime;

public record UserEvent (
        Long id,
        String action,
        String entityType,
        Long entityId,
        Long userId,
        String username,
        String details,
        LocalDateTime timestamp
){
}
