package com.adoptapp.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.adoptapp.userservice.model.UserStatus;
import com.adoptapp.userservice.model.User;
import jakarta.validation.constraints.NotNull;

public record UserRequest(

        @NotBlank(message = "El username es requerido")
        @Size(min = 3, max = 50)
        String username,

        @NotBlank(message = "El nombre es requerido")
        @Size(max = 50)
        String name,

        @NotBlank(message = "El apellido es requerido")
        @Size(max = 50)
        String surname,

        @NotBlank(message = "El email es requerido")
        @Email(message = "El email no es válido")
        String email,

        @NotBlank(message = "La contraseña es requerida")
        @Size(min = 6, max = 100)
        String password,

        @NotBlank(message = "El teléfono es requerido")
        String phone,

        @NotBlank(message = "El país es requerido")
        String country,

        @NotBlank(message = "La ciudad es requerida")
        String city,

        @NotBlank(message = "La calle es requerida")
        String street,

        @NotBlank(message = "El número es requerido")
        String homeNumber,

        @NotBlank(message = "El código postal es requerido")
        String postalCode,

        @NotBlank(message = "El tipo de dirección es requerido")
        String type,

        @NotNull(message = "El status es requerido")
        UserStatus status,

        User.Role role,

        boolean active
) {
}