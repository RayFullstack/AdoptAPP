package com.adoptapp.shelterservice.dto;

public record StaffResponse(
        Long id,
        Long userId,
        Long shelterId,
        String position,
        String phone,
        String email,
        String hireDate,
        String status,
        String createdAt,
        String updatedAt
) {
}
