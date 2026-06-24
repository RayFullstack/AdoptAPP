package com.adoptapp.userservice.controller;

import com.adoptapp.sharedkernel.dto.ErrorResponse;
import com.adoptapp.sharedkernel.dto.UserAuthResponse;
import com.adoptapp.userservice.dto.*;
import com.adoptapp.userservice.service.UserLinkAssembler;
import com.adoptapp.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Tag(name = "Usuarios", description = "Manejo de usuarios")
@RestController
@RequestMapping("/users")
@SecurityRequirement(name = "basicAuth")
@Validated
public class UserController {

    private final UserService service;
    private final UserLinkAssembler linkAssembler;

    public UserController(UserService service, UserLinkAssembler linkAssembler) {
        this.service = service;
        this.linkAssembler = linkAssembler;
    }

    @Operation(summary = "Listar usuarios no inactivos", description = "Devuelve usuarios con enlaces HATEOAS en _links")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuarios listados correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Estado de usuario invÃƒÆ’Ã‚Â¡lido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para listar usuarios",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<UserResponse>>> getAllUsers(
            @Parameter(
                    description = "Estado por el cual filtrar los usuarios",
                    example = "ACTIVE",
                    schema = @Schema(allowableValues = {"ACTIVE", "INACTIVE", "SUSPENDED"})
            )
            @RequestParam(required = false) String status) {

        List<UserResult> results = status != null
                ? this.service.getUsers(status)
                : this.service.getUsers();
        CollectionModel<EntityModel<UserResponse>> collection = toCollectionModel(results);
        collection.add(linkTo(methodOn(UserController.class).getAllUsers(status)).withSelfRel());
        return ResponseEntity.ok(collection);
    }


    @Operation(summary = "Buscar usuario por ID", description = "Devuelve el usuario con enlaces HATEOAS en _links")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario obtenido correctamente"),
            @ApiResponse(responseCode = "400", description = "ID de usuario invÃƒÆ’Ã‚Â¡lido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para ver este usuario",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontrÃƒÆ’Ã‚Â³ al usuario con ese ID",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/by-id/{id}")
    public ResponseEntity<EntityModel<UserResponse>> getUserById(@PathVariable Long id) {
        return toResponseEntity(this.service.getById(id));
    }


    @Operation(summary = "Buscar usuario por email", description = "Devuelve el usuario con enlaces HATEOAS en _links")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario obtenido correctamente"),
            @ApiResponse(responseCode = "400", description = "Correo de usuario invÃƒÆ’Ã‚Â¡lido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para ver este usuario",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontrÃƒÆ’Ã‚Â³ al usuario con este correo electrÃƒÆ’Ã‚Â³nico",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/by-email/{email}")
    public ResponseEntity<EntityModel<UserResponse>> getUserByEmail(
            @PathVariable @Email(message = "El correo electrÃƒÆ’Ã‚Â³nico no es vÃƒÆ’Ã‚Â¡lido") String email) {
        return toResponseEntity(this.service.getByEmail(email));
    }


    @Hidden
    @Operation(summary = "Listar Usuarios y Hash por mail", description = "Obtiene un usuario y su clave hasheada por su mail")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Credenciales de usuario obtenidas correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para ver las credenciales del usuario",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontrÃƒÆ’Ã‚Â³ un usuario con ese correo electrÃƒÆ’Ã‚Â³nico",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/by-email/{email}/auth")
    public ResponseEntity<UserAuthResponse> getUserAuthByEmail(
            @PathVariable @Email(message = "El correo electrÃƒÆ’Ã‚Â³nico no es vÃƒÆ’Ã‚Â¡lido") String email) {
        return this.service.getAuthByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado"));
    }


    @Operation(summary = "Listar historial de cambios de un usuario",
            description = "Obtiene el historial de cambios realizados sobre un usuario mediante su ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historial del usuario obtenido correctamente"),
            @ApiResponse(responseCode = "400", description = "ID de usuario invÃƒÆ’Ã‚Â¡lido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para ver el historial de cambios del usuario",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontrÃƒÆ’Ã‚Â³ un usuario con ese ID",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/by-id/{id}/history")
    public ResponseEntity<List<UserHistoryResponse>> getHistory(@PathVariable Long id) {
        return service.getHistory(id)
                .map(ResponseEntity::ok)
                .orElse(com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado"));
    }


    @Operation(summary = "Crear usuario", description = "Crea un usuario y devuelve enlaces HATEOAS en _links")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos invÃƒÆ’Ã‚Â¡lidos, username o email duplicado, o rol no permitido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para crear este usuario",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<EntityModel<UserResponse>> create(@Valid @RequestBody UserRequest request) {
        UserCommand command = toCommand(request);
        UserResult result = this.service.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(linkAssembler.toModel(toResponse(result)));
    }


    @Operation(summary = "Registrar usuario", description = "Registra un usuario y devuelve enlaces HATEOAS en _links")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario registrado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos de usuario invÃƒÆ’Ã‚Â¡lidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/register")
    @SecurityRequirements
    public ResponseEntity<EntityModel<UserResponse>> registerUser(@Valid @RequestBody UserRequest request) {
        UserCommand command = toCommand(request);
        UserResult result = this.service.register(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(linkAssembler.toModel(toResponse(result)));
    }


    @Operation(summary = "Actualizar usuario por ID", description = "Actualiza un usuario y devuelve enlaces HATEOAS en _links")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "ID o datos de usuario invÃƒÆ’Ã‚Â¡lidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para actualizar este usuario",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontrÃƒÆ’Ã‚Â³ un usuario con ese ID",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/by-id/{id}")
    @PreAuthorize("@userSecurity.canEdit(#id, authentication)")
    public ResponseEntity<EntityModel<UserResponse>> updateUserById(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request) {
        UserCommand command = toCommand(request);
        return toResponseEntity(this.service.updateById(id, command));
    }


    @Operation(summary = "Eliminar Usuarios por ID", description = "Elimina un usuario por su id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuario eliminado correctamente"),
            @ApiResponse(responseCode = "400", description = "ID de usuario invÃƒÆ’Ã‚Â¡lido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para eliminar este usuario",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontrÃƒÆ’Ã‚Â³ un usuario con ese ID",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
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
    private CollectionModel<EntityModel<UserResponse>> toCollectionModel(List<UserResult> results) {
        List<EntityModel<UserResponse>> users = results.stream()
                .map(this::toResponse)
                .map(linkAssembler::toModel)
                .toList();
        return CollectionModel.of(users);
    }

    private ResponseEntity<EntityModel<UserResponse>> toResponseEntity(Optional<UserResult> result) {
        return result
                .map(this::toResponse)
                .map(linkAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElse(com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado"));
    }
}

