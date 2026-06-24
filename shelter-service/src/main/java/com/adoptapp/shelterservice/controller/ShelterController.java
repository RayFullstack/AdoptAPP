package com.adoptapp.shelterservice.controller;

import com.adoptapp.sharedkernel.dto.ErrorResponse;

import com.adoptapp.shelterservice.dto.*;
import com.adoptapp.shelterservice.service.ShelterLinkAssembler;
import com.adoptapp.shelterservice.service.ShelterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Tag(name = "Refugios", description = "Operaciones para gestionar refugios")
@RestController
@RequestMapping("/shelters")
@SecurityRequirement(name = "basicAuth")
public class ShelterController {

    private final ShelterService service;
    private final ShelterLinkAssembler linkAssembler;

    public ShelterController(ShelterService service, ShelterLinkAssembler linkAssembler) {
        this.service = service;
        this.linkAssembler = linkAssembler;
    }

    @Operation(summary = "Listar refugios", description = "Devuelve refugios con enlaces HATEOAS en _links")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Refugios listados correctamente"),
            @ApiResponse(responseCode = "400", description = "Estado de refugio invalido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para listar refugios", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<ShelterResponse>>> getAllShelters(
            @RequestParam(required = false) String status) {

        List<ShelterResult> results = status != null
                ? this.service.getShelters(status)
                : this.service.getShelters();

        CollectionModel<EntityModel<ShelterResponse>> collection = toCollectionModel(results);
        collection.add(linkTo(methodOn(ShelterController.class).getAllShelters(status)).withSelfRel());
        return ResponseEntity.ok(collection);
    }

    @Operation(summary = "Buscar refugio por ID", description = "Devuelve un refugio con enlaces HATEOAS en _links")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Refugio encontrado correctamente"),
            @ApiResponse(responseCode = "400", description = "ID de refugio invalido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para ver este refugio", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontro el refugio con ese ID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/by-id/{id}")
    public ResponseEntity<EntityModel<ShelterResponse>> getShelterById(
            @PathVariable Long id) {

        return toResponseEntity(this.service.getById(id));
    }

    @GetMapping("/by-id/{id}/history")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historial de refugio obtenido correctamente"),
            @ApiResponse(responseCode = "400", description = "ID de refugio invalido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para ver el historial de este refugio", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontro el refugio con ese ID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<ShelterHistoryResponse>> getHistory(
            @PathVariable Long id) {
        List<ShelterHistoryResponse> history = this.service.getHistory(id);
        return ResponseEntity.ok(history);
    }

    @Operation(summary = "Crear refugio", description = "Crea un refugio y devuelve enlaces HATEOAS en _links")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Refugio creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos de refugio invalidos o email no valido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para crear refugios", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "El refugio no cumple una regla de negocio", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Servicio remoto no disponible", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<EntityModel<ShelterResponse>> create(
            @Valid @RequestBody ShelterRequest request,
            Authentication authentication) {

        ShelterCommand command = toCommand(request);
        Long userId = getAuthenticatedUserId(authentication);

        ShelterResult result = this.service.create(command, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(linkAssembler.toModel(toResponse(result)));
    }

    @Operation(summary = "Actualizar refugio por ID", description = "Actualiza un refugio y devuelve enlaces HATEOAS en _links")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Refugio actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "ID o datos de refugio invalidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para actualizar este refugio", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontro el refugio con ese ID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "El refugio no cumple una regla de negocio", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Servicio remoto no disponible", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/by-id/{id}")
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<EntityModel<ShelterResponse>> updateShelterById(
            @PathVariable Long id,
            @Valid @RequestBody ShelterRequest request,
            Authentication authentication) {

        if (!canModifyShelter(id, authentication)) {
            return com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
        }

        ShelterCommand command = toCommand(request);
        Long userId = getAuthenticatedUserId(authentication);
        return toResponseEntity(this.service.updateById(id, command, userId));
    }

    @DeleteMapping("/by-id/{id}")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Refugio eliminado correctamente"),
            @ApiResponse(responseCode = "400", description = "ID de refugio invalido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para eliminar este refugio", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontro el refugio con ese ID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<Void> deleteShelterById(
            @PathVariable Long id,
            Authentication authentication) {

        if (!canModifyShelter(id, authentication)) {
            return com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
        }

        Long userId = getAuthenticatedUserId(authentication);
        if (!this.service.deleteById(id, userId)) {
            return com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
        }

        return ResponseEntity.noContent().build();
    }

    private ShelterCommand toCommand(ShelterRequest request) {
        return new ShelterCommand(
                request.name(),
                request.email(),
                request.phone(),
                request.description(),
                request.status()
        );
    }

    private ShelterResponse toResponse(ShelterResult result) {
        return new ShelterResponse(
                result.id(),
                result.name(),
                result.email(),
                result.phone(),
                result.description(),
                result.status(),
                result.active(),
                result.createdAt(),
                result.updatedAt()
        );
    }

    private CollectionModel<EntityModel<ShelterResponse>> toCollectionModel(List<ShelterResult> results) {
        List<EntityModel<ShelterResponse>> shelters = results.stream()
                .map(this::toResponse)
                .map(linkAssembler::toModel)
                .toList();
        return CollectionModel.of(shelters);
    }

    private ResponseEntity<EntityModel<ShelterResponse>> toResponseEntity(Optional<ShelterResult> result) {
        return result
                .map(this::toResponse)
                .map(linkAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElse(com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado"));
    }

    private boolean canModifyShelter(Long shelterId, Authentication authentication) {
        if (hasRole(authentication, "ROLE_ADMIN")) {
            return true;
        }

        Long authenticatedShelterId = getShelterIdForAuthenticatedUser(authentication);
        return authenticatedShelterId.equals(shelterId)
                && this.service.getByIdActive(shelterId).isPresent();
    }

    private Long getAuthenticatedUserId(Authentication authentication) {
        return service.getUserIdByEmail(authentication.getName());
    }

    private Long getShelterIdForAuthenticatedUser(Authentication authentication) {
        Long userId = getAuthenticatedUserId(authentication);
        return service.getShelterIdForStaffUser(userId);
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }
}






