package com.adoptapp.petservice.controller;

import com.adoptapp.sharedkernel.dto.ErrorResponse;

import com.adoptapp.petservice.dto.*;
import com.adoptapp.petservice.service.PetLinkAssembler;
import com.adoptapp.petservice.service.PetService;
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

@Tag(name = "Mascotas", description = "Operaciones para gestionar mascotas")
@RestController
@RequestMapping("/pets")
@SecurityRequirement(name = "basicAuth")
public class PetController {
    private final PetService service;
    private final PetLinkAssembler linkAssembler;

    public PetController(PetService service, PetLinkAssembler linkAssembler) {
        this.service = service;
        this.linkAssembler = linkAssembler;
    }

    @Operation(
            summary = "Listar mascotas",
            description = "Devuelve las mascotas disponibles con enlaces HATEOAS en _links"
    )
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<PetResponse>>> getAllPets() {
        CollectionModel<EntityModel<PetResponse>> collection = toCollectionModel(this.service.getPets());
        collection.add(linkTo(methodOn(PetController.class).getAllPets()).withSelfRel());
        return ResponseEntity.ok(collection);
    }

    @Operation(
            summary = "Listar mascotas activas por refugio",
            description = "Devuelve las mascotas activas de un refugio con enlaces HATEOAS en _links"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mascotas activas por refugio listadas correctamente"),
            @ApiResponse(responseCode = "400", description = "Estado de mascota invalido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para listar estos registros", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/internal/shelter/{shelterId}/active")
    public ResponseEntity<CollectionModel<EntityModel<PetResponse>>> getActivePetsByShelter(@PathVariable Long shelterId) {
        CollectionModel<EntityModel<PetResponse>> collection = toCollectionModel(this.service.getPetsByShelter(shelterId));
        collection.add(linkTo(methodOn(PetController.class).getActivePetsByShelter(shelterId)).withSelfRel());
        return ResponseEntity.ok(collection);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ficha de salud de mascota obtenida correctamente"),
            @ApiResponse(responseCode = "400", description = "ID de mascota o refugio invalido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para ver la salud de esta mascota", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/by-id/{id}/health")
    public ResponseEntity<com.adoptapp.petservice.dto.HealthResult> getPetHealth(@PathVariable Long id) {
        return service.getHealthInfo(id)
                .map(ResponseEntity::ok)
                .orElse(com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado"));
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historial de mascota obtenido correctamente"),
            @ApiResponse(responseCode = "400", description = "ID de mascota o refugio invalido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para ver el historial de esta mascota", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/by-id/{id}/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHELTER_ADMIN')")
    public ResponseEntity<List<PetHistoryResult>> getHistory(
            @PathVariable Long id,
            Authentication authentication) {

        if (!canViewPetIncludingDeleted(id, authentication)) {
            return com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
        }

        return service.getHistory(id)
                .map(ResponseEntity::ok)
                .orElse(com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado"));
    }

    @Operation(
            summary = "Buscar mascota por ID",
            description = "Devuelve la mascota con enlaces HATEOAS en _links"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mascota encontrada correctamente"),
            @ApiResponse(responseCode = "400", description = "ID de mascota o refugio invalido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para ver esta mascota", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/by-id/{id}")
    public ResponseEntity<EntityModel<PetResponse>> getPetById(@PathVariable Long id) {
        return toResponseEntity(this.service.getById(id));
    }

    @Operation(
            summary = "Buscar mascota por ID incluyendo eliminadas",
            description = "Devuelve la mascota con enlaces HATEOAS en _links, incluyendo registros eliminados si el rol lo permite"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mascota encontrada correctamente"),
            @ApiResponse(responseCode = "400", description = "ID de mascota o refugio invalido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para ver esta mascota", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/admin/by-id/{id}")
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<EntityModel<PetResponse>> getByIdIncludingDeleted(
            @PathVariable Long id,
            Authentication authentication) {

        if (isAdmin(authentication)) {
            return toResponseEntity(this.service.getByIdIncludingDeleted(id));
        }

        Long shelterId = getShelterIdForAuthenticatedUser(authentication);
        return toResponseEntity(this.service.getByIdIncludingDeletedForShelter(id, shelterId));
    }

    @Operation(
            summary = "Listar mascotas por estado",
            description = "Devuelve las mascotas visibles por estado con enlaces HATEOAS en _links"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mascotas por estado listadas correctamente"),
            @ApiResponse(responseCode = "400", description = "Estado de mascota invalido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para listar estos registros", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "La mascota no cumple una regla de negocio", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Servicio remoto no disponible", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<CollectionModel<EntityModel<PetResponse>>> getPetsByStatusAdmin(
            @RequestParam(required = false) String status,
            Authentication authentication) {

        CollectionModel<EntityModel<PetResponse>> collection = toCollectionModel(getVisibleAdminPets(status, authentication));
        collection.add(linkTo(methodOn(PetController.class)
                .getPetsByStatusAdmin(status, authentication))
                .withSelfRel());

        return ResponseEntity.ok(collection);
    }

    @Operation(
            summary = "Crear mascota",
            description = "Crea una mascota y devuelve la respuesta con enlaces HATEOAS en _links"
    )
    @PostMapping
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'VOLUNTEER', 'ADMIN')")
    public ResponseEntity<EntityModel<PetResponse>> create(
            @Valid @RequestBody PetRequest request,
            Authentication authentication) {
        PetCommand command = toCommand(request);
        Long shelterId = isAdmin(authentication)
                ? null
                : getShelterIdForAuthenticatedUser(authentication);

        PetResult result = isAdmin(authentication)
                ? this.service.create(command)
                : this.service.createForShelter(command, shelterId);
        return ResponseEntity.status(HttpStatus.CREATED).body(linkAssembler.toModel(toResponse(result)));
    }

    @Operation(
            summary = "Actualizar mascota por ID",
            description = "Actualiza una mascota y devuelve la respuesta con enlaces HATEOAS en _links"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mascota actualizada correctamente"),
            @ApiResponse(responseCode = "400", description = "ID o datos de mascota invalidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para actualizar este recurso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "La mascota no cumple una regla de negocio", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Servicio remoto no disponible", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/by-id/{id}")
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'VOLUNTEER', 'ADMIN')")
    public ResponseEntity<EntityModel<PetResponse>> updatePetById(
            @PathVariable Long id,
            @Valid @RequestBody PetRequest request,
            Authentication authentication) {

        PetCommand command = toCommand(request);
        if (!isAdmin(authentication) && command.shelterId() == null) {
            return com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
        }

        if (!canModifyPet(id, command.shelterId(), authentication)) {
            return com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
        }

        if (isAdmin(authentication)) {
            return toResponseEntity(this.service.updateById(id, command));
        }

        Long shelterId = getShelterIdForAuthenticatedUser(authentication);
        return toResponseEntity(this.service.updateByIdForShelter(id, command, shelterId));
    }

    @Operation(
            summary = "Actualizar estado de mascota",
            description = "Actualiza el estado de una mascota y devuelve la respuesta con enlaces HATEOAS en _links"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado de mascota actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "ID o datos de mascota invalidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para actualizar este recurso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "La mascota no cumple una regla de negocio", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Servicio remoto no disponible", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/by-id/{id}/status")
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'VOLUNTEER', 'ADMIN')")
    public ResponseEntity<EntityModel<PetResponse>> updatePetByStatus(@PathVariable Long id,
                                                    @Valid @RequestBody PetStatusRequest request,
                                                    Authentication authentication) {
        if (!canModifyPet(id, null, authentication)) {
            return com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
        }

        return toResponseEntity(this.service.updateByStatus(id, request.status()));
    }

    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Mascota eliminada correctamente"),
            @ApiResponse(responseCode = "400", description = "ID de mascota o refugio invalido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para eliminar este recurso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Recurso no encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "La mascota no cumple una regla de negocio", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Servicio remoto no disponible", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/by-id/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePetById(@PathVariable Long id) {
        boolean deleted = this.service.deleteById(id);
        return deleted
                ? ResponseEntity.noContent().build()
                : com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
    }

    private PetCommand toCommand(PetRequest request) {
        return new PetCommand(
                request.name(),
                request.species(),
                request.race(),
                request.age(),
                request.size(),
                request.color(),
                request.personality(),
                request.status(),
                request.shelterId()
        );
    }

    private PetResponse toResponse(PetResult result) {
        return new PetResponse(
                result.id(),
                result.name(),
                result.species(),
                result.race(),
                result.age(),
                result.size(),
                result.color(),
                result.status(),
                result.personality(),
                result.shelterId()
        );
    }

    private CollectionModel<EntityModel<PetResponse>> toCollectionModel(List<PetResult> results) {
        List<EntityModel<PetResponse>> pets = results.stream()
                .map(this::toResponse)
                .map(linkAssembler::toModel)
                .toList();

        return CollectionModel.of(pets);
    }

    private ResponseEntity<EntityModel<PetResponse>> toResponseEntity(Optional<PetResult> result) {
        return result
                .map(this::toResponse)
                .map(linkAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElse(com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado"));
    }

    private boolean canModifyPet(Long id, Long requestedShelterId, Authentication authentication) {
        if (isAdmin(authentication)) {
            return true;
        }

        Long shelterId = getShelterIdForAuthenticatedUser(authentication);
        if (requestedShelterId != null && !shelterId.equals(requestedShelterId)) {
            return false;
        }

        return this.service.getByIdForShelter(id, shelterId).isPresent();
    }

    private boolean canViewPetIncludingDeleted(Long id, Authentication authentication) {
        if (isAdmin(authentication)) {
            return true;
        }

        Long shelterId = getShelterIdForAuthenticatedUser(authentication);
        return this.service.getByIdIncludingDeletedForShelter(id, shelterId).isPresent();
    }

    private List<PetResult> getVisibleAdminPets(String status, Authentication authentication) {
        if (isAdmin(authentication)) {
            return status != null
                    ? this.service.getPets(status)
                    : this.service.getPets();
        }

        Long shelterId = getShelterIdForAuthenticatedUser(authentication);
        return status != null
                ? this.service.getPetsByShelter(shelterId, status)
                : this.service.getPetsByShelter(shelterId);
    }

    private Long getShelterIdForAuthenticatedUser(Authentication authentication) {
        Long userId = service.getUserIdByEmail(authentication.getName());
        return service.getShelterIdForStaffUser(userId);
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }

    private boolean isAdmin(Authentication authentication) {
        return hasRole(authentication, "ROLE_ADMIN");
    }
}










