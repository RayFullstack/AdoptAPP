package com.adoptapp.adoptionservice.controller;

import com.adoptapp.adoptionservice.dto.*;
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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "Adopciones", description = "Operaciones para gestionar adopciones")
@RestController
@RequestMapping("/adoptions")
@SecurityRequirement(name = "basicAuth")
public class AdoptionController {

    private final AdoptionService service;

    public AdoptionController(AdoptionService service) {
        this.service = service;
    }

    @Operation(summary = "Listar todas las adopciones")
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
    public ResponseEntity<List<AdoptionResponse>> getAllAdoptions(Authentication authentication) {
        return ResponseEntity.ok(toResponseList(getVisibleAdoptions(authentication)));
    }


    @Operation(summary = "Buscar adopción por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Adopción encontrada correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para ver esta adopción",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontró la adopción con ese ID",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/by-id/{id}")
    @PreAuthorize("hasAnyRole('ADOPTER', 'SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<AdoptionResponse> getAdoptionById(
            @PathVariable Long id,
            Authentication authentication) {

        if (hasRole(authentication, "ROLE_ADMIN")) {
            return toResponseEntity(this.service.getById(id));
        }

        if (hasRole(authentication, "ROLE_SHELTER_ADMIN")) {
            Long shelterId = getShelterIdForAuthenticatedUser(authentication);
            return toResponseEntity(this.service.getByIdForShelter(id, shelterId));
        }

        Long userId = service.getUserIdByEmail(authentication.getName());
        return toResponseEntity(this.service.getByIdForUser(id, userId));
    }

    @Operation(summary = "Buscar adopción por ID incluyendo canceladas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Adopción encontrada correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para ver esta adopción",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Adopción no encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/admin/by-id/{id}")
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<AdoptionResponse> getByIdIncludingCancelled(
            @PathVariable Long id,
            Authentication authentication) {

        if (hasRole(authentication, "ROLE_ADMIN")) {
            return toResponseEntity(this.service.getByIdIncludingCancelled(id));
        }

        Long shelterId = getShelterIdForAuthenticatedUser(authentication);
        return toResponseEntity(this.service.getByIdIncludingCancelledForShelter(id, shelterId));
    }

    @Operation(summary = "Listar adopciones por status")
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
    public ResponseEntity<List<AdoptionResponse>> getAdoptionsByStatusAdmin(
            @RequestParam(required = false) String status,
            Authentication authentication) {

        return ResponseEntity.ok(toResponseList(getVisibleAdminAdoptions(status, authentication)));
    }

    @Operation(summary = "Listar historial de cambios de adopción por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historial de adopción listado correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para ver el historial de cambios de " +
                    "esta adopción",content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontraron historial para esta adopción",
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

    @Operation(summary = "Crear adopción")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Adopción creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para crear esta adopción",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Mascota, usuario o refugio no encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "La adopción no cumple una regla de negocio",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Servicio remoto no disponible",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'ADMIN', 'ADOPTER')")
    public ResponseEntity<AdoptionResponse> createAdoption(
            @Valid @RequestBody AdoptionCreateRequest request,
            Authentication authentication) {

        AdoptionCommand command = toCommand(request, authentication);
        AdoptionResult result = canCreateWithoutShelterRestriction(authentication)
                ? this.service.create(command)
                : this.service.createForShelterAdmin(command, getShelterIdForAuthenticatedUser(authentication));
        AdoptionResponse response = toResponse(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Actualizar adopción por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Adopción actualizada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para actualizar esta adopción",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontró la adopción con ese ID",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "La actualización no cumple una regla de negocio",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Servicio remoto no disponible",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/by-id/{id}")
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<AdoptionResponse> updateAdoptionById(
            @PathVariable Long id,
            @Valid @RequestBody AdoptionUpdateRequest request,
            Authentication authentication) {

        AdoptionCommand command = toCommand(request);
        if (!canShelterAdminModifyActiveAdoption(id, authentication)) {
            return com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
        }

        return toResponseEntity(this.service.updateById(id, command));
    }

    @Operation(summary = "Eliminar adopción por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Adopción eliminada correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para eliminar esta adopción",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontró la adopción con ese ID",
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

    private ResponseEntity<AdoptionResponse> toResponseEntity(Optional<AdoptionResult> result) {
        return result
                .map(this::toResponse)
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
