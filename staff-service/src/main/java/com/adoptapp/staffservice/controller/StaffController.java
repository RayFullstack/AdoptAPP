package com.adoptapp.staffservice.controller;

import com.adoptapp.sharedkernel.dto.ErrorResponse;

import com.adoptapp.staffservice.dto.*;
import com.adoptapp.staffservice.service.StaffLinkAssembler;
import com.adoptapp.staffservice.service.StaffService;
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

@Tag(name = "Staff", description = "Operaciones para gestionar trabajadores de refugio")
@RestController
@RequestMapping("/staff")
@SecurityRequirement(name = "basicAuth")
public class StaffController {

    private final StaffService service;
    private final StaffLinkAssembler linkAssembler;

    public StaffController(StaffService service, StaffLinkAssembler linkAssembler) {
        this.service = service;
        this.linkAssembler = linkAssembler;
    }

    @Operation(summary = "Listar staff", description = "Devuelve staff con enlaces HATEOAS en _links")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Staff listado correctamente"),
            @ApiResponse(responseCode = "400", description = "Estado o cargo de staff invalido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para listar staff de este refugio", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<StaffResponse>>> getAllStaff(
            @RequestParam(required = false) String status,
            Authentication authentication) {

        CollectionModel<EntityModel<StaffResponse>> collection = toCollectionModel(getVisibleStaff(status, authentication));
        collection.add(linkTo(methodOn(StaffController.class).getAllStaff(status, authentication)).withSelfRel());
        return ResponseEntity.ok(collection);
    }

    @Operation(summary = "Buscar staff por ID", description = "Devuelve un registro de staff con enlaces HATEOAS en _links")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Staff encontrado correctamente"),
            @ApiResponse(responseCode = "400", description = "ID de staff, usuario o refugio invalido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para ver este staff", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontro el registro de staff solicitado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/by-id/{id}")
    public ResponseEntity<EntityModel<StaffResponse>> getStaffById(
            @PathVariable Long id,
            Authentication authentication) {

        return toResponseEntity(getVisibleStaffById(id, authentication));
    }

    @Operation(summary = "Buscar staff por usuario", description = "Devuelve staff por usuario con enlaces HATEOAS en _links")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Staff por usuario encontrado correctamente"),
            @ApiResponse(responseCode = "400", description = "ID de staff, usuario o refugio invalido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para ver este staff", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontro el registro de staff solicitado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/by-user/{userId}")
    public ResponseEntity<EntityModel<StaffResponse>> getStaffByUserId(@PathVariable Long userId) {
        return toResponseEntity(this.service.getByUserId(userId));
    }

    @Operation(summary = "Listar staff activo por refugio", description = "Devuelve staff activo por refugio con enlaces HATEOAS en _links")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Staff activo por refugio listado correctamente"),
            @ApiResponse(responseCode = "400", description = "Estado o cargo de staff invalido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para listar staff de este refugio", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/internal/shelter/{shelterId}/active")
    public ResponseEntity<CollectionModel<EntityModel<StaffResponse>>> getActiveStaffByShelter(@PathVariable Long shelterId) {
        CollectionModel<EntityModel<StaffResponse>> collection = toCollectionModel(this.service.getAllStaffByShelter(shelterId));
        collection.add(linkTo(methodOn(StaffController.class).getActiveStaffByShelter(shelterId)).withSelfRel());
        return ResponseEntity.ok(collection);
    }

    @GetMapping("/by-id/{id}/history")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historial de staff obtenido correctamente"),
            @ApiResponse(responseCode = "400", description = "ID de staff, usuario o refugio invalido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para ver el historial de este staff", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontro el registro de staff solicitado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StaffHistoryResponse>> getHistory(@PathVariable Long id) {
        List<StaffHistoryResponse> history = this.service.getHistory(id);
        return ResponseEntity.ok(history);
    }

    @Operation(summary = "Crear staff", description = "Crea staff y devuelve enlaces HATEOAS en _links")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Staff creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos de staff invalidos, usuario o refugio no valido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para crear staff en este refugio", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "El staff no cumple una regla de negocio", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Servicio remoto no disponible", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<EntityModel<StaffResponse>> create(
            @Valid @RequestBody StaffRequest request,
            Authentication authentication) {

        if (!canUseShelter(request.shelterId(), authentication)) {
            return com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
        }

        StaffCommand command = toCommand(request);
        StaffResult result = this.service.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(linkAssembler.toModel(toResponse(result)));
    }

    @Operation(summary = "Actualizar staff por ID", description = "Actualiza staff y devuelve enlaces HATEOAS en _links")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Staff actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "ID o datos de staff invalidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para actualizar este staff", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontro el registro de staff solicitado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "El staff no cumple una regla de negocio", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Servicio remoto no disponible", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/by-id/{id}")
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<EntityModel<StaffResponse>> updateStaffById(
            @PathVariable Long id,
            @Valid @RequestBody StaffRequest request,
            Authentication authentication) {

        if (!canModifyStaff(id, authentication) || !canUseShelter(request.shelterId(), authentication)) {
            return com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
        }

        StaffCommand command = toCommand(request);
        return toResponseEntity(this.service.updateById(id, command));
    }

    @DeleteMapping("/by-id/{id}")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Staff desactivado correctamente"),
            @ApiResponse(responseCode = "400", description = "ID de staff, usuario o refugio invalido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para desactivar este staff", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontro el registro de staff solicitado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<Void> deleteStaffById(
            @PathVariable Long id,
            Authentication authentication) {

        if (!canModifyStaff(id, authentication)) {
            return com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
        }

        if (!this.service.deleteById(id)) {
            return com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
        }

        return ResponseEntity.noContent().build();
    }

    private StaffCommand toCommand(StaffRequest request) {
        return new StaffCommand(
                request.userId(),
                request.shelterId(),
                request.position(),
                request.phone(),
                request.email(),
                request.hireDate(),
                request.status()
        );
    }

    private StaffResponse toResponse(StaffResult result) {
        return new StaffResponse(
                result.id(),
                result.userId(),
                result.shelterId(),
                result.position(),
                result.phone(),
                result.email(),
                result.hireDate(),
                result.status(),
                result.createdAt(),
                result.updatedAt()
        );
    }

    private CollectionModel<EntityModel<StaffResponse>> toCollectionModel(List<StaffResult> results) {
        List<EntityModel<StaffResponse>> staff = results.stream()
                .map(this::toResponse)
                .map(linkAssembler::toModel)
                .toList();
        return CollectionModel.of(staff);
    }

    private ResponseEntity<EntityModel<StaffResponse>> toResponseEntity(Optional<StaffResult> result) {
        return result
                .map(this::toResponse)
                .map(linkAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElse(com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado"));
    }

    private List<StaffResult> getVisibleStaff(String status, Authentication authentication) {
        if (hasRole(authentication, "ROLE_ADMIN")) {
            return status != null
                    ? this.service.getAllStaff(status)
                    : this.service.getAllStaff();
        }

        Long shelterId = getShelterIdForAuthenticatedUser(authentication);
        return this.service.getAllStaffByShelter(shelterId).stream()
                .filter(staff -> status == null || staff.status().name().equalsIgnoreCase(status))
                .toList();
    }

    private Optional<StaffResult> getVisibleStaffById(Long id, Authentication authentication) {
        if (hasRole(authentication, "ROLE_ADMIN")) {
            return this.service.getById(id);
        }

        Long shelterId = getShelterIdForAuthenticatedUser(authentication);
        return this.service.getByIdForShelter(id, shelterId);
    }

    private boolean canModifyStaff(Long id, Authentication authentication) {
        return getVisibleStaffById(id, authentication).isPresent();
    }

    private boolean canUseShelter(Long shelterId, Authentication authentication) {
        if (hasRole(authentication, "ROLE_ADMIN")) {
            return true;
        }

        Long authenticatedShelterId = getShelterIdForAuthenticatedUser(authentication);
        return authenticatedShelterId.equals(shelterId);
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








