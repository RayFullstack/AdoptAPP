package com.adoptapp.userservice.dto;

import com.adoptapp.userservice.model.User;
import com.adoptapp.userservice.model.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;


@Schema(description = "Información de un usuario")
public record UserResponse (
        @Schema(description = "ID del usuario", example = "2")
        Long id,

        @Schema(description = "Nombre de usuario", example = "benito123")
        String username,

        @Schema(description = "Nombre de pila del usuario", example = "Benito")
        String name,

        @Schema(description = "Apellido del usuario", example = "Suarez")
        String surname,

        @Schema(description = "Correo electrónico del usuario", example = "benitochampion@mail.com")
        String email,

        @Schema(description = "Número de teléfono del usuario", example = "1234456")
        String phone,

        @Schema(description = "País de origen del usuario", example = "Chile")
        String country,

        @Schema(description = "Ciudad de residencia del usuario", example = "Santiago")
        String city,

        @Schema(description = "Nombre de la calle de residencia del usuario", example = "Calle 1")
        String street,

        @Schema(description = "Número de la calle de residencia del usuario", example = "222")
        String homeNumber,

        @Schema(description = "Código postal del usuario", example = "32801")
        String postalCode,

        @Schema(description = "Tipo de dirección del usuario", example = "HOME",
            allowableValues = {"HOME", "WORK"})
        String type,

        @Schema(description = "Estado del usuario", example = "SUSPENDED",
                 allowableValues = {"ACTIVE", "SUSPENDED", "INACTIVE"})
        UserStatus status,

        @Schema(description = "Rol del usuario", example = "ADOPTER",
                 allowableValues = {"ADOPTER", "SHELTER_ADMIN", "VOLUNTEER", "VET", "ADMIN" })
        User.Role role,

        @Schema(description = "Indica si el usuario está activo", example = "false")
        boolean active
){
}
