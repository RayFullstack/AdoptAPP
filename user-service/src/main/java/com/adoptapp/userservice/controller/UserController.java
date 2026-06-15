package com.adoptapp.userservice.controller;

import com.adoptapp.sharedkernel.dto.UserAuthResponse;
import com.adoptapp.userservice.dto.*;
import com.adoptapp.userservice.model.UserStatus;
import com.adoptapp.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
@Tag(name = "Usuarios", description = "Manejo de usuarios")
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @Operation(summary = "Listar Usuarios", description = "Obtiene todos los usuarios registrados")
    @ApiResponse(responseCode = "200", description = "Usuarios obtenidos correctamente")
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers(
            @RequestParam(required = false) String status) {

        List<UserResult> results = status != null
                ? this.service.getUsers(status)
                : this.service.getUsers();
        List<UserResponse> responses = results.stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Listar Usuarios por ID", description = "Obtiene un usuario por su id")
    @ApiResponse(responseCode = "200", description = "Usuario obtenido correctamente")
    @GetMapping("/by-id/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return this.service.getById(id)
                .map(result -> toResponse(result))
                .map(ResponseEntity::ok)
                .orElse(com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado"));
    }

    @Operation(summary = "Listar Usuarios por mail", description = "Obtiene un usuario por su mail")
    @ApiResponse(responseCode = "200", description = "Usuario obtenido correctamente")
    @GetMapping("/by-email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        return this.service.getByEmail(email)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado"));
    }

    @Operation(summary = "Listar Usuarios y Hash por mail", description = "Obtiene un usuario y su clave hasheada por su mail")
    @ApiResponse(responseCode = "200", description = "Usuario obtenido correctamente")
    @GetMapping("/by-email/{email}/auth")
    public ResponseEntity<UserAuthResponse> getUserAuthByEmail(@PathVariable String email) {
        return this.service.getAuthByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado"));
    }

    @Operation(summary = "Listar Historial de cambios de un usuario", description = "Obtiene el historial de cambios hecho a un usuario por su id")
    @ApiResponse(responseCode = "200", description = "Usuario obtenido correctamente")
    @GetMapping("/by-id/{id}/history")
    public ResponseEntity<List<UserHistoryResult>> getHistory(@PathVariable Long id) {
        return service.getHistory(id)
                .map(ResponseEntity::ok)
                .orElse(com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado"));
    }

    @Operation(summary = "Crear Usuario", description = "Crea un usuario con mail, id, nombre de usuario, etc. ")
    @ApiResponse(responseCode = "200", description = "Usuario obtenido correctamente")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        UserCommand command = toCommand(request);
        UserResult result = this.service.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(result));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRequest request) {
        UserCommand command = toCommand(request);
        UserResult result = this.service.register(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(result));
    }

    @Operation(summary = "Actualizar Usuarios por ID", description = "Actualiza un usuario por su id")
    @ApiResponse(responseCode = "200", description = "Usuario actualizado correctamente")
    @PutMapping("/by-id/{id}")
    @PreAuthorize("@userSecurity.canEdit(#id, authentication)")
    public ResponseEntity<UserResponse> updateUserById(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request) {
        UserCommand command = toCommand(request);
        return this.service.updateById(id, command)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado"));
    }

    @Operation(summary = "Eliminar Usuarios por ID", description = "Elimina un usuario por su id")
    @ApiResponse(responseCode = "200", description = "Usuario eliminado correctamente")
    @DeleteMapping("/by-id/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUserById(@PathVariable Long id) {
        if (!this.service.deleteById(id)) {
            return com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
        }
        return ResponseEntity.noContent().build();
    }

    private UserCommand toCommand(UserRequest request) {
        return new UserCommand(
                request.username(),
                request.name(),
                request.surname(),
                request.email(),
                request.password(),
                request.phone(),
                request.country(),
                request.city(),
                request.street(),
                request.homeNumber(),
                request.postalCode(),
                request.type(),
                request.status(),
                request.role(),
                request.active()
        );
    }

    private UserResponse toResponse(UserResult result) {
        return new UserResponse(
                result.id(),
                result.username(),
                result.name(),
                result.surname(),
                result.email(),
                result.phone(),
                result.country(),
                result.city(),
                result.street(),
                result.homeNumber(),
                result.postalCode(),
                result.type(),
                result.status(),
                result.role(),
                result.active()
        );
    }
}

