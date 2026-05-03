package com.adoptapp.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record UserRequest (@NotBlank(message = "El nombre de usuario es requerido")
                          @Size(min = 3, max = 50)
                          String username,
                           @NotBlank(message = "El nombre es requerido")
                           @Size(min = 1, max = 50)
                          String name,
                           @NotBlank(message = "El apellido es requerido")
                           @Size(min = 1, max = 50)
                           String surname,
                           @NotBlank(message = "El telefono es requerido")
                           @Size(min = 8, max = 15)
                           String phone,
                           @NotBlank(message = "La dirección es requerida")
                           @Size(min = 8, max = 15)
                           String address,
                           @Email(message = "El correo electronico debe ser valido")
                           @Size(min = 1, max = 50)
                           String email,
                           String status){
}
