package com.adoptapp.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.adoptapp.userservice.model.UserStatus;
import com.adoptapp.userservice.model.User;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Datos para crear o actualizar de un usuario")
public record UserRequest(

        @Schema(description = "nombre de usuario", example = "benitoelmaquina")
        @NotBlank(message = "El username es requerido")
        @Size(min = 3, max = 50)
        String username,

        @Schema(description = "Nombre de pila del usuario", example = "Benito")
        @NotBlank(message = "El nombre es requerido")
        @Size(max = 50)
        String name,

        @Schema(description = "Apellido del usuario", example = "Suarez")
        @NotBlank(message = "El apellido es requerido")
        @Size(max = 50)
        String surname,

        @Schema(description = "Correo electrónico del usuario", example = "benitoelmaquina@mail.com")
        @NotBlank(message = "El email es requerido")
        @Email(message = "El email no es válido")
        String email,

        @Schema(description = "contraseña del usuario", example = "usuario123",
                format = "password", accessMode = Schema.AccessMode.WRITE_ONLY)
        @NotBlank(message = "La contraseña es requerida")
        @Size(min = 6, max = 100)
        String password,

        @Schema(description = "Número del usuario", example = "123345662")
        @NotBlank(message = "El teléfono es requerido")
        String phone,

        @Schema(description = "País de residencia del usuario", example = "Chile")
        @NotBlank(message = "El país es requerido")
        String country,

        @Schema(description = "Ciudad de residencia del usuario", example = "Santiago")
        @NotBlank(message = "La ciudad es requerida")
        String city,

        @Schema(description = "Calle de residencia del usuario", example = "Calle 67")
        @NotBlank(message = "La calle es requerida")
        String street,

        @Schema(description = "Numéro de la residencia del usuario", example = "111")
        @NotBlank(message = "El número es requerido")
        String homeNumber,

        @Schema(description = "Código postal del usuario", example = "8250000")
        @NotBlank(message = "El código postal es requerido")
        String postalCode,

        @Schema(description = "Tipo de dirección del usuario", example = "HOME",
        allowableValues = {"HOME", "WORK"})
        @NotBlank(message = "El tipo de dirección es requerido")
        String type,

        @Schema(description = "Estado del usuario", example = "ACTIVE",
                allowableValues = {"ACTIVE", "INACTIVE", "SUSPENDED"})
        @NotNull(message = "El status es requerido")
        UserStatus status,

        @Schema(description = "Rol del usuario", example = "VOLUNTEER",
                allowableValues = {"ADOPTER", "SHELTER_ADMIN", "VOLUNTEER", "VET"})
        User.Role role,

        @Schema(description = "Indica si el usuario está activo", example = "true")
        boolean active
) {
}