package com.adoptapp.supplyservice.controller;

import com.adoptapp.sharedkernel.dto.ErrorResponse;

import com.adoptapp.supplyservice.dto.*;
import com.adoptapp.supplyservice.service.SupplyLinkAssembler;
import com.adoptapp.supplyservice.service.SupplyService;
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

@Tag(name = "Insumos", description = "Operaciones para gestionar insumos")
@RestController
@RequestMapping("/supplies")
@SecurityRequirement(name = "basicAuth")
public class SupplyController {

    private final SupplyService supplyService;
    private final SupplyLinkAssembler linkAssembler;

    public SupplyController(SupplyService supplyService, SupplyLinkAssembler linkAssembler) {
        this.supplyService = supplyService;
        this.linkAssembler = linkAssembler;
    }

    @Operation(summary = "Listar insumos", description = "Devuelve insumos con enlaces HATEOAS en _links")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Insumos listados correctamente"),
            @ApiResponse(responseCode = "400", description = "Estado, categoria o filtro de insumo invalido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para listar insumos de este refugio", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<SupplyResponse>>> getAllSupplies(
            @RequestParam(required = false) String status,
            Authentication authentication) {

        CollectionModel<EntityModel<SupplyResponse>> collection = toCollectionModel(getVisibleSupplies(status, authentication));
        collection.add(linkTo(methodOn(SupplyController.class).getAllSupplies(status, authentication)).withSelfRel());
        return ResponseEntity.ok(collection);
    }

    @Operation(summary = "Buscar insumo por ID", description = "Devuelve un insumo con enlaces HATEOAS en _links")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Insumo encontrado correctamente"),
            @ApiResponse(responseCode = "400", description = "ID de insumo o refugio invalido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para ver este insumo", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontro el insumo solicitado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/by-id/{id}")
    public ResponseEntity<EntityModel<SupplyResponse>> getSupplyById(
            @PathVariable Long id,
            Authentication authentication) {
        return toResponseEntity(getVisibleSupplyById(id, authentication));
    }

    @Operation(summary = "Listar insumos por refugio", description = "Devuelve insumos de un refugio con enlaces HATEOAS en _links")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Insumos por refugio listados correctamente"),
            @ApiResponse(responseCode = "400", description = "Estado, categoria o filtro de insumo invalido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para listar insumos de este refugio", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/shelter/{shelterId}")
    public ResponseEntity<CollectionModel<EntityModel<SupplyResponse>>> getSuppliesByShelter(
            @PathVariable Long shelterId,
            Authentication authentication) {

        if (!canUseShelter(shelterId, authentication)) {
            return com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
        }

        CollectionModel<EntityModel<SupplyResponse>> collection = toCollectionModel(supplyService.findByShelterId(shelterId));
        collection.add(linkTo(methodOn(SupplyController.class).getSuppliesByShelter(shelterId, authentication)).withSelfRel());
        return ResponseEntity.ok(collection);
    }

    @Operation(summary = "Listar insumos activos por refugio", description = "Devuelve insumos activos de un refugio con enlaces HATEOAS en _links")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Insumos activos por refugio listados correctamente"),
            @ApiResponse(responseCode = "400", description = "Estado, categoria o filtro de insumo invalido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para listar insumos de este refugio", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/internal/shelter/{shelterId}/active")
    public ResponseEntity<CollectionModel<EntityModel<SupplyResponse>>> getActiveSuppliesByShelter(@PathVariable Long shelterId) {
        CollectionModel<EntityModel<SupplyResponse>> collection = toCollectionModel(supplyService.findByShelterId(shelterId));
        collection.add(linkTo(methodOn(SupplyController.class).getActiveSuppliesByShelter(shelterId)).withSelfRel());
        return ResponseEntity.ok(collection);
    }

    @GetMapping("/by-id/{id}/history")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historial de insumo obtenido correctamente"),
            @ApiResponse(responseCode = "400", description = "ID de insumo o refugio invalido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para ver el historial de este insumo", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontro el insumo solicitado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SupplyHistoryResponse>> getSupplyHistory(@PathVariable Long id) {
        return supplyService.getHistory(id)
                .map(ResponseEntity::ok)
                .orElse(com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado"));
    }

    @Operation(summary = "Crear insumo", description = "Crea un insumo y devuelve enlaces HATEOAS en _links")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Insumo creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos de insumo invalidos, usuario o refugio no valido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para crear insumos en este refugio", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "El insumo no cumple una regla de negocio", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Servicio remoto no disponible", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SHELTER_ADMIN')")
    public ResponseEntity<EntityModel<SupplyResponse>> createSupply(
            @Valid @RequestBody SupplyRequest request,
            Authentication authentication) {

        if (!canUseShelter(request.shelterId(), authentication)) {
            return com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
        }

        SupplyCommand command = toCommand(request);
        SupplyResult result = supplyService.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(linkAssembler.toModel(toResponse(result)));
    }

    @Operation(summary = "Actualizar insumo por ID", description = "Actualiza un insumo y devuelve enlaces HATEOAS en _links")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Insumo actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "ID o datos de insumo invalidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para actualizar este insumo", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontro el insumo solicitado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "El insumo no cumple una regla de negocio", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Servicio remoto no disponible", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/by-id/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHELTER_ADMIN')")
    public ResponseEntity<EntityModel<SupplyResponse>> updateSupply(@PathVariable Long id,
                                                       @Valid @RequestBody SupplyRequest request,
                                                       Authentication authentication) {
        if (!canModifySupply(id, authentication) || !canUseShelter(request.shelterId(), authentication)) {
            return com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
        }

        SupplyCommand command = toCommand(request);
        return toResponseEntity(supplyService.update(id, command));
    }

    @DeleteMapping("/by-id/{id}")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Insumo eliminado correctamente"),
            @ApiResponse(responseCode = "400", description = "ID de insumo o refugio invalido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para eliminar este insumo", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontro el insumo solicitado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'SHELTER_ADMIN')")
    public ResponseEntity<Void> deleteSupply(
            @PathVariable Long id,
            Authentication authentication) {

        if (!canModifySupply(id, authentication)) {
            return com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
        }

        boolean deleted = supplyService.delete(id);
        return deleted
                ? ResponseEntity.noContent().build()
                : com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
    }

    private SupplyCommand toCommand(SupplyRequest request) {
        return new SupplyCommand(
                request.name(), request.description(), request.quantity(),
                request.unit(), request.category(), request.shelterId(),
                request.userId(), request.supplierName(),
                request.minimumStock(), request.status()
        );
    }

    private SupplyResponse toResponse(SupplyResult result) {
        return new SupplyResponse(
                result.id(), result.name(), result.description(), result.quantity(),
                result.unit(), result.category(), result.shelterId(),
                result.supplierName(), result.minimumStock(), result.status(),
                result.createdAt(), result.updatedAt()
        );
    }

    private CollectionModel<EntityModel<SupplyResponse>> toCollectionModel(List<SupplyResult> results) {
        List<EntityModel<SupplyResponse>> supplies = results.stream()
                .map(this::toResponse)
                .map(linkAssembler::toModel)
                .toList();
        return CollectionModel.of(supplies);
    }

    private ResponseEntity<EntityModel<SupplyResponse>> toResponseEntity(Optional<SupplyResult> result) {
        return result
                .map(this::toResponse)
                .map(linkAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElse(com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado"));
    }

    private List<SupplyResult> getVisibleSupplies(String status, Authentication authentication) {
        if (hasRole(authentication, "ROLE_ADMIN")) {
            return status != null
                    ? supplyService.getSupplies(status)
                    : supplyService.getSupplies();
        }

        if (!hasShelterScopedRole(authentication)) {
            return status != null
                    ? supplyService.getSupplies(status)
                    : supplyService.getSupplies();
        }

        Long shelterId = getShelterIdForAuthenticatedUser(authentication);
        return supplyService.findByShelterId(shelterId, status);
    }

    private Optional<SupplyResult> getVisibleSupplyById(Long id, Authentication authentication) {
        if (hasRole(authentication, "ROLE_ADMIN")) {
            return supplyService.getById(id);
        }

        if (!hasShelterScopedRole(authentication)) {
            return supplyService.getById(id);
        }

        Long shelterId = getShelterIdForAuthenticatedUser(authentication);
        return supplyService.getByIdForShelter(id, shelterId);
    }

    private boolean canModifySupply(Long id, Authentication authentication) {
        return getVisibleSupplyById(id, authentication).isPresent();
    }

    private boolean canUseShelter(Long shelterId, Authentication authentication) {
        if (hasRole(authentication, "ROLE_ADMIN")) {
            return true;
        }

        if (!hasShelterScopedRole(authentication)) {
            return true;
        }

        Long authenticatedShelterId = getShelterIdForAuthenticatedUser(authentication);
        return authenticatedShelterId.equals(shelterId);
    }

    private Long getShelterIdForAuthenticatedUser(Authentication authentication) {
        Long userId = supplyService.getUserIdByEmail(authentication.getName());
        return supplyService.getShelterIdForStaffUser(userId);
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }

    private boolean hasShelterScopedRole(Authentication authentication) {
        return hasRole(authentication, "ROLE_SHELTER_ADMIN")
                || hasRole(authentication, "ROLE_VOLUNTEER")
                || hasRole(authentication, "ROLE_VET");
    }
}








