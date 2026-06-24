package com.adoptapp.notificationservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Informacion del trabajador obtenida desde staff-service")
public record StaffResponse(
        @Schema(description = "ID del registro", example = "1")
        Long id,
        @Schema(description = "ID del usuario asociado", example = "1")
        Long userId,
        @Schema(description = "ID del refugio asociado", example = "2")
        Long shelterId,
        @Schema(description = "Cargo del trabajador", example = "VET")
        String position,
        @Schema(description = "Numero de telefono", example = "912345678")
        String phone,
        @Schema(description = "Correo electronico", example = "usuario@mail.com")
        String email,
        @Schema(description = "Fecha y hora de contratacion", example = "2026-04-21T14:30:00")
        String hireDate,
        @Schema(description = "Estado actual del registro", example = "ACTIVE")
        String status,
        @Schema(description = "Fecha y hora de creacion del registro", example = "2026-04-21T14:30:00")
        String createdAt,
        String updatedAt
) {
}
