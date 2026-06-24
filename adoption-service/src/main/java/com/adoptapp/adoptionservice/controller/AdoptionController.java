package com.adoptapp.adoptionservice.controller;

import com.adoptapp.adoptionservice.dto.*;
import com.adoptapp.adoptionservice.service.AdoptionLinkAssembler;
import com.adoptapp.adoptionservice.service.AdoptionService;

import com.adoptapp.sharedkernel.dto.ErrorResponse;
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

@Tag(name = "Adopciones", description = "Operaciones para gestionar adopciones")
@RestController
@RequestMapping("/adoptions")
@SecurityRequirement(name = "basicAuth")
public class AdoptionController {

    private final AdoptionService service;
    private final AdoptionLinkAssembler linkAssembler;

    public AdoptionController(AdoptionService service, AdoptionLinkAssembler linkAssembler) {
        this.service = service;
        this.linkAssembler = linkAssembler;
    }

    @Operation(summary = "Listar todas las adopciones", description = "Devuelve adopciones con enlaces HATEOAS en _links")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Adopciones listadas correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para listar adopciones",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADOPTER', 'SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<CollectionModel<EntityModel<AdoptionResponse>>> getAllAdoptions(Authentication authentication) {
        List<EntityModel<AdoptionResponse>> adoptions = getVisibleAdoptions(authentication).stream()
                .map(this::toResponse)
                .map(linkAssembler::toModel)
                .toList();

        CollectionModel<EntityModel<AdoptionResponse>> collection =
                CollectionModel.of(adoptions);

        collection.add(linkTo(methodOn(AdoptionController.class)
                .getAllAdoptions(authentication))
                .withSelfRel());

        return ResponseEntity.ok(collection);
    }


    @Operation(summary = "Buscar adopcion por ID", description = "Devuelve la adopcion con enlaces HATEOAS en _links")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Adopcion encontrada correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para ver esta adopcion",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontro la adopcion con ese ID",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/by-id/{id}")
    @PreAuthorize("hasAnyRole('ADOPTER', 'SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<EntityModel<AdoptionResponse>> getAdoptionById(
            @PathVariable Long id,
            Authentication authentication) {

        Optional<AdoptionResult> result;

        if (hasRole(authentication, "ROLE_ADMIN")) {
            result = this.service.getById(id);
        } else if (hasRole(authentication, "ROLE_SHELTER_ADMIN")) {
            Long shelterId = getShelterIdForAuthenticatedUser(authentication);
            result = this.service.getByIdForShelter(id, shelterId);
        } else {
            Long userId = service.getUserIdByEmail(authentication.getName());
            result = this.service.getByIdForUser(id, userId);
        }

        return result
                .map(this::toResponse)
                .map(linkAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Buscar adopcion por ID incluyendo canceladas", description = "Devuelve la adopcion con enlaces HATEOAS en _links")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Adopcion encontrada correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para ver esta adopcion",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Adopcion no encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/admin/by-id/{id}")
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<EntityModel<AdoptionResponse>> getByIdIncludingCancelled(
            @PathVariable Long id,
            Authentication authentication) {

        if (hasRole(authentication, "ROLE_ADMIN")) {
            return toResponseEntity(this.service.getByIdIncludingCancelled(id));
        }

        Long shelterId = getShelterIdForAuthenticatedUser(authentication);
        return toResponseEntity(this.service.getByIdIncludingCancelledForShelter(id, shelterId));
    }

    @Operation(summary = "Listar adopciones por status", description = "Devuelve adopciones con enlaces HATEOAS en _links")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Adopciones listadas correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para ver estas adopciones",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<CollectionModel<EntityModel<AdoptionResponse>>> getAdoptionsByStatusAdmin(
            @RequestParam(required = false) String status,
            Authentication authentication) {

        List<EntityModel<AdoptionResponse>> adoptions = getVisibleAdminAdoptions(status, authentication).stream()
                .map(this::toResponse)
                .map(linkAssembler::toModel)
                .toList();

        CollectionModel<EntityModel<AdoptionResponse>> collection = CollectionModel.of(adoptions);
        collection.add(linkTo(methodOn(AdoptionController.class)
                .getAdoptionsByStatusAdmin(status, authentication))
                .withSelfRel());

        return ResponseEntity.ok(collection);
    }

    @Operation(summary = "Listar historial de cambios de adopcion por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historial de adopcion listado correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para ver el historial de cambios de " +
                    "esta adopcion",content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontraron historial para esta adopcion",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/by-id/{id}/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADOPTER', 'SHELTER_ADMIN')")
    public ResponseEntity<List<AdoptionHistoryResponse>> getHistory(@PathVariable Long id, Authentication authentication) {

        if (hasRole(authentication, "ROLE_ADMIN")) {
            return ResponseEntity.ok(service.getHistory(id));
        }

        if (!canViewAdoption(id, authentication)) {
            return com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
        }

        return ResponseEntity.ok(service.getHistory(id));
    }

    @Operation(summary = "Crear adopcion", description = "Crea una adopcion y devuelve enlaces HATEOAS en _links")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Adopcion creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para crear esta adopcion",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Mascota, usuario o refugio no encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "La adopcion no cumple una regla de negocio",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Servicio remoto no disponible",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'ADMIN', 'ADOPTER')")
    public ResponseEntity<EntityModel<AdoptionResponse>> createAdoption(
            @Valid @RequestBody AdoptionCreateRequest request,
            Authentication authentication) {

        AdoptionCommand command = toCommand(request, authentication);
        AdoptionResult result = canCreateWithoutShelterRestriction(authentication)
                ? this.service.create(command)
                : this.service.createForShelterAdmin(command, getShelterIdForAuthenticatedUser(authentication));
        EntityModel<AdoptionResponse> response = linkAssembler.toModel(toResponse(result));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Actualizar adopcion por ID", description = "Actualiza una adopcion y devuelve enlaces HATEOAS en _links")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Adopcion actualizada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos invalidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para actualizar esta adopcion",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontro la adopcion con ese ID",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "La actualizacion no cumple una regla de negocio",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Servicio remoto no disponible",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/by-id/{id}")
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<EntityModel<AdoptionResponse>> updateAdoptionById(
            @PathVariable Long id,
            @Valid @RequestBody AdoptionUpdateRequest request,
            Authentication authentication) {

        AdoptionCommand command = toCommand(request);
        if (!canShelterAdminModifyActiveAdoption(id, authentication)) {
            return com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
        }

        return toResponseEntity(this.service.updateById(id, command));
    }

    @Operation(summary = "Eliminar adopcion por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Adopcion eliminada correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para eliminar esta adopcion",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontro la adopcion con ese ID",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/by-id/{id}")
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<Void> deleteAdoptionById(
            @PathVariable Long id, Authentication authentication) {

        if (!canShelterAdminModifyAnyAdoption(id, authentication)) {
            return com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
        }

        boolean deleted = this.service.deleteById(id);

        return deleted
                ? ResponseEntity.noContent().build()
                : com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
    }

    private List<AdoptionResult> getVisibleAdoptions(Authentication authentication) {
        if (hasRole(authentication, "ROLE_ADMIN")) {
            return this.service.getAdoptions();
        }

        if (hasRole(authentication, "ROLE_SHELTER_ADMIN")) {
            Long shelterId = getShelterIdForAuthenticatedUser(authentication);
            return this.service.getAdoptionsByShelter(shelterId);
        }

        Long userId = service.getUserIdByEmail(authentication.getName());
        return this.service.getAdoptionsByUser(userId);
    }

    private List<AdoptionResult> getVisibleAdminAdoptions(String status, Authentication authentication) {
        if (hasRole(authentication, "ROLE_ADMIN")) {
            return status != null
                    ? this.service.getAdoptionsIncludingCancelledByStatus(status)
                    : this.service.getAdoptions();
        }

        Long shelterId = getShelterIdForAuthenticatedUser(authentication);
        return status != null
                ? this.service.getAdoptionsByShelter(shelterId, status)
                : this.service.getAdoptionsByShelter(shelterId);
    }

    private boolean canViewAdoption(Long id, Authentication authentication) {
        if (hasRole(authentication, "ROLE_SHELTER_ADMIN")) {
            Long shelterId = getShelterIdForAuthenticatedUser(authentication);
            return this.service.getByIdForShelter(id, shelterId).isPresent();
        }

        Long userId = service.getUserIdByEmail(authentication.getName());
        return this.service.getByIdForUser(id, userId).isPresent();
    }

    private boolean canShelterAdminModifyActiveAdoption(Long id, Authentication authentication) {
        if (!hasRole(authentication, "ROLE_SHELTER_ADMIN")) {
            return true;
        }

        Long shelterId = getShelterIdForAuthenticatedUser(authentication);
        return this.service.getByIdForShelter(id, shelterId).isPresent();
    }

    private boolean canShelterAdminModifyAnyAdoption(Long id, Authentication authentication) {
        if (!hasRole(authentication, "ROLE_SHELTER_ADMIN")) {
            return true;
        }

        Long shelterId = getShelterIdForAuthenticatedUser(authentication);
        return this.service.getByIdIncludingCancelledForShelter(id, shelterId).isPresent();
    }

    private AdoptionCommand toCommand(AdoptionCreateRequest request, Authentication authentication) {

        return new AdoptionCommand(
                getUserIdForCreate(request, authentication),
                request.petId(),
                null

        );
    }

    private AdoptionCommand toCommand(AdoptionUpdateRequest request) {
        return new AdoptionCommand(
                null,
                null,
                request.status()
        );
    }

    private Long getUserIdForCreate(AdoptionCreateRequest request, Authentication authentication) {
        if (isOnlyAdopter(authentication)) {
            return service.getUserIdByEmail(authentication.getName());
        }

        if (request.userId() == null) {
            throw new IllegalArgumentException("El userId es requerido");
        }

        return request.userId();
    }

    private boolean isOnlyAdopter(Authentication authentication) {
        return hasRole(authentication, "ROLE_ADOPTER")
                && !hasRole(authentication, "ROLE_ADMIN")
                && !hasRole(authentication, "ROLE_SHELTER_ADMIN");
    }

    private boolean canCreateWithoutShelterRestriction(Authentication authentication) {
        return hasRole(authentication, "ROLE_ADMIN")
                || isOnlyAdopter(authentication);
    }

    private AdoptionResponse toResponse(AdoptionResult result) {

        return new AdoptionResponse(
                result.id(),
                result.userId(),
                result.petId(),
                result.status(),
                result.createdAt(),
                result.updatedAt()
        );
    }

    private List<AdoptionResponse> toResponseList(List<AdoptionResult> results) {
        return results.stream()
                .map(this::toResponse)
                .toList();
    }

    private ResponseEntity<EntityModel<AdoptionResponse>> toResponseEntity(Optional<AdoptionResult> result) {
        return result
                .map(this::toResponse)
                .map(linkAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElse(com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado"));
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
