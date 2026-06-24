package com.adoptapp.donationservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import com.adoptapp.donationservice.model.DonationStatus;

import java.math.BigDecimal;

@Schema(description = "Datos internos para procesar una donacion")
public record DonationCommand(

        @Schema(description = "Nombre del donante", example = "Camila Soto")
        String donorName,
        @Schema(description = "Monto asociado al registro", example = "25000")
        BigDecimal amount,
        @Schema(description = "Descripcion del registro", example = "Descripcion del registro")
        String description,
        @Schema(description = "Estado actual del registro", example = "ACTIVE")
        DonationStatus status,
        @Schema(description = "ID del usuario asociado", example = "1")
        Long userId,
        Long shelterId
) {
}
