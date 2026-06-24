package com.adoptapp.donationservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Datos para crear una donacion")
public record DonationCreateRequest(
                @NotBlank(message = "Donor name is required")
                @Schema(description = "Nombre del donante", example = "Camila Soto")
                String donorName,

                @NotNull(message = "Amount is required")
                @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
                @Schema(description = "Monto asociado al registro", example = "25000")
                BigDecimal amount,

                @NotBlank(message = "Description is required")
                @Schema(description = "Descripcion del registro", example = "Descripcion del registro")
                String description,

                @NotNull(message = "User ID is required")
                @Schema(description = "ID del usuario asociado", example = "1")
                Long userId,

                @NotNull(message = "Shelter ID is required")
                Long shelterId
) {
}
