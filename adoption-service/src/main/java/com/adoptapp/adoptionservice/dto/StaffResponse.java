package com.adoptapp.adoptionservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Información de un trabajador de un refugio obtenida desde staff-service")
public record StaffResponse(
        @Schema(description = "ID del trabajador ", example = "44")
        Long id,

        @Schema(description = "ID del usuario asociado al trabajador", example = "43")
        Long userId,

        @Schema(description = "ID del refugio donde trabaja", example = "67")
        Long shelterId,

        @Schema(description = "Cargo del trabajador", example = "VET")
        String position,

        @Schema(description = "Numero de telefono del trabajador", example = "123456789")
        String phone,

        @Schema(description = "Email del trabajador", example = "trabajador@mail.com")
        String email,

        @Schema(description = "Fecha y hora de contratación del trabajador", example = "2026-04-21T14:30:00")
        String hireDate,

        @Schema(description = "Estado actual del trabajador", example = "ACTIVE",
                allowableValues = {"ACTIVE", "INACTIVE", "SUSPENDED"})
        String status,

        @Schema(description = "Fecha y hora de creación del registro del trabajador", example = "2026-04-21T14:30:00")
        String createdAt,

        @Schema(description = "Fecha y hora de la última actualización del registro del trabajador", example = "2026-04-22T10:15:00")
        String updatedAt
) {
}
