package com.adoptapp.healthservice.controller;

import com.adoptapp.sharedkernel.dto.ErrorResponse;

import com.adoptapp.healthservice.dto.*;
import com.adoptapp.healthservice.service.HealthLinkAssembler;
import com.adoptapp.healthservice.service.HealthService;
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

@Tag(name = "Salud", description = "Operaciones para gestionar fichas de salud")
@RestController
@RequestMapping("/health")
@SecurityRequirement(name = "basicAuth")
public class HealthController {

    private final HealthService service;
    private final HealthLinkAssembler linkAssembler;

    public HealthController(HealthService service, HealthLinkAssembler linkAssembler) {
        this.service = service;
        this.linkAssembler = linkAssembler;
    }

    @Operation(summary = "Listar fichas de salud", description = "Devuelve fichas de salud con enlaces HATEOAS en _links")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fichas de salud listadas correctamente"),
            @ApiResponse(responseCode = "400", description = "Estado de vacunacion o esterilizacion invalido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para listar fichas de salud", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<HealthResponse>>> getAllHealth(
            @RequestParam(required = false) String vaccinationStatus,
            @RequestParam(required = false) String sterilizationStatus) {

        List<HealthResult> results;
        if (vaccinationStatus != null) {
            results = this.service.getVax(vaccinationStatus);
        } else if (sterilizationStatus != null) {
            results = this.service.getSter(sterilizationStatus);
        } else {
            results = this.service.getHealth();
        }

        CollectionModel<EntityModel<HealthResponse>> collection = toCollectionModel(results);
        collection.add(linkTo(methodOn(HealthController.class)
                .getAllHealth(vaccinationStatus, sterilizationStatus))
                .withSelfRel());
        return ResponseEntity.ok(collection);
    }

    @Operation(summary = "Buscar ficha de salud por ID", description = "Devuelve una ficha de salud con enlaces HATEOAS en _links")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ficha de salud encontrada correctamente"),
            @ApiResponse(responseCode = "400", description = "ID de ficha de salud o mascota invalido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para ver esta ficha de salud", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontro la ficha de salud solicitada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/by-id/{id}")
    public ResponseEntity<EntityModel<HealthResponse>> getHealthById(@PathVariable Long id) {
        return toResponseEntity(this.service.getById(id));
    }

    @Operation(summary = "Buscar ficha de salud por mascota", description = "Devuelve la ficha de salud de una mascota con enlaces HATEOAS en _links")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ficha de salud por mascota encontrada correctamente"),
            @ApiResponse(responseCode = "400", description = "ID de ficha de salud o mascota invalido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para ver esta ficha de salud", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontro la ficha de salud solicitada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/by-pet/{petId}")
    public ResponseEntity<EntityModel<HealthResponse>> getHealthByPetId(@PathVariable Long petId) {
        return toResponseEntity(this.service.getByPetId(petId));
    }

    @GetMapping("/by-id/{id}/history")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historial de salud obtenido correctamente"),
            @ApiResponse(responseCode = "400", description = "ID de ficha de salud o mascota invalido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para ver el historial de esta ficha de salud", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontro la ficha de salud solicitada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<HealthHistoryResponse>> getHistory(@PathVariable Long id) {
        List<HealthHistoryResponse> history = this.service.getHistory(id);
        return ResponseEntity.ok(history);
    }

    @Operation(summary = "Crear ficha de salud", description = "Crea una ficha de salud y devuelve enlaces HATEOAS en _links")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ficha de salud creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos de ficha de salud invalidos, usuario o mascota no valida", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para crear fichas de salud", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "La ficha de salud no cumple una regla de negocio", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Servicio remoto no disponible", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('VET', 'SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<EntityModel<HealthResponse>> createForm(
            @Valid @RequestBody HealthRequest request,
            Authentication authentication) {

        if (!canUsePet(request.petId(), authentication)) {
            return com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
        }

        HealthCommand command = toCommand(request);
        HealthResult result = this.service.create(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(linkAssembler.toModel(toResponse(result)));
    }

    @Operation(summary = "Actualizar ficha de salud por ID", description = "Actualiza una ficha de salud y devuelve enlaces HATEOAS en _links")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ficha de salud actualizada correctamente"),
            @ApiResponse(responseCode = "400", description = "ID o datos de ficha de salud invalidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para actualizar esta ficha de salud", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontro la ficha de salud solicitada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "La ficha de salud no cumple una regla de negocio", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Servicio remoto no disponible", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/by-id/{id}")
    @PreAuthorize("hasAnyRole('VET', 'SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<EntityModel<HealthResponse>> updateById(
            @PathVariable Long id,
            @Valid @RequestBody HealthRequest request,
            Authentication authentication) {

        if (!canModifyHealth(id, authentication) || !canUsePet(request.petId(), authentication)) {
            return com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
        }

        HealthCommand command = toCommand(request);
        return toResponseEntity(this.service.updateById(id, command));
    }

    @DeleteMapping("/by-id/{id}")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Ficha de salud eliminada correctamente"),
            @ApiResponse(responseCode = "400", description = "ID de ficha de salud o mascota invalido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para eliminar esta ficha de salud", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontro la ficha de salud solicitada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "La ficha de salud no cumple una regla de negocio", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Servicio remoto no disponible", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasAnyRole('VET', 'SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<Void> deleteById(
            @PathVariable Long id,
            Authentication authentication) {

        if (!canModifyHealth(id, authentication)) {
            return com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
        }

        boolean deleted = service.deleteById(id);
        if (!deleted) {
            return com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
        }
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/by-pet/{petId}")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Ficha de salud de mascota eliminada correctamente"),
            @ApiResponse(responseCode = "400", description = "ID de ficha de salud o mascota invalido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para eliminar esta ficha de salud", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontro la ficha de salud solicitada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasAnyRole('VET', 'SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<Void> deleteByPetId(
            @PathVariable Long petId,
            Authentication authentication) {

        if (!canUsePet(petId, authentication)) {
            return com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
        }

        boolean deleted = service.deleteByPetId(petId);
        if (!deleted) {
            return com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
        }
        return ResponseEntity.noContent().build();
    }

    private HealthCommand toCommand(HealthRequest request) {
        return new HealthCommand(
                request.userId(),
                request.petId(),
                request.vaccinationStatus(),
                request.sterilizationStatus(),
                request.diseases()
        );
    }

    private HealthResponse toResponse(HealthResult result) {
        return new HealthResponse(
                result.id(),
                result.userId(),
                result.petId(),
                result.vaccinationStatus(),
                result.sterilizationStatus(),
                result.diseases(),
                result.status(),
                result.createdAt(),
                result.updatedAt()
        );
    }

    private CollectionModel<EntityModel<HealthResponse>> toCollectionModel(List<HealthResult> results) {
        List<EntityModel<HealthResponse>> health = results.stream()
                .map(this::toResponse)
                .map(linkAssembler::toModel)
                .toList();
        return CollectionModel.of(health);
    }

    private ResponseEntity<EntityModel<HealthResponse>> toResponseEntity(Optional<HealthResult> result) {
        return result
                .map(this::toResponse)
                .map(linkAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElse(com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado"));
    }

    private boolean canModifyHealth(Long healthId, Authentication authentication) {
        if (hasRole(authentication, "ROLE_ADMIN")) {
            return true;
        }

        return this.service.getById(healthId)
                .map(health -> canUsePet(health.petId(), authentication))
                .orElse(false);
    }

    private boolean canUsePet(Long petId, Authentication authentication) {
        if (hasRole(authentication, "ROLE_ADMIN")) {
            return true;
        }

        Long shelterId = getShelterIdForAuthenticatedUser(authentication);
        return this.service.petBelongsToShelter(petId, shelterId);
    }

    private Long getShelterIdForAuthenticatedUser(Authentication authentication) {
        Long userId = service.getUserIdByEmail(authentication.getName());
        return service.getShelterIdForStaffUser(userId);
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }
}








