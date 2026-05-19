package com.adoptapp.staffservice.dto;

import com.adoptapp.staffservice.model.StaffPosition;
import com.adoptapp.staffservice.model.StaffStatus;

import java.time.LocalDateTime;

public record StaffResponse(
        Long id,
        Long userId,
        Long shelterId,
        StaffPosition position,
        String phone,
        String email,
        LocalDateTime hireDate,
        StaffStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
