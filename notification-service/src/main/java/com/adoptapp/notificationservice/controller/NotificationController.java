package com.adoptapp.notificationservice.controller;

import com.adoptapp.sharedkernel.dto.ErrorResponse;

import com.adoptapp.notificationservice.dto.*;
import com.adoptapp.notificationservice.service.NotificationLinkAssembler;
import com.adoptapp.notificationservice.service.NotificationService;
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

@Tag(name = "Notificaciones", description = "Operaciones para gestionar notificaciones")
@RestController
@RequestMapping("/notifications")
@SecurityRequirement(name = "basicAuth")
public class NotificationController {

    private final NotificationService service;
    private final NotificationLinkAssembler linkAssembler;

    public NotificationController(NotificationService service, NotificationLinkAssembler linkAssembler) {
        this.service = service;
        this.linkAssembler = linkAssembler;
    }

    @Operation(summary = "Listar notificaciones", description = "Devuelve notificaciones con enlaces HATEOAS en _links")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notificaciones listadas correctamente"),
            @ApiResponse(responseCode = "400", description = "Estado de notificacion invalido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para listar estas notificaciones", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADOPTER', 'VOLUNTEER', 'VET', 'SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<CollectionModel<EntityModel<NotificationResponse>>> getAllNotifications(
            @RequestParam(required = false) String status,
            Authentication authentication) {

        List<NotificationResult> results;

        if (hasRole(authentication, "ROLE_ADMIN")) {
            results = status != null
                    ? this.service.getNotifications(status)
                    : this.service.getNotifications();
        } else if (hasRole(authentication, "ROLE_SHELTER_ADMIN")) {
            Long userId = service.getUserIdByEmail(authentication.getName());
            Long shelterId = service.getShelterIdForStaffUser(userId);
            results = status != null
                    ? this.service.getNotificationsByUserOrShelter(userId, shelterId, status)
                    : this.service.getNotificationsByUserOrShelter(userId, shelterId);
        } else {
            Long userId = service.getUserIdByEmail(authentication.getName());
            results = status != null
                    ? this.service.getNotificationsByUser(userId, status)
                    : this.service.getNotificationsByUser(userId);
        }

        CollectionModel<EntityModel<NotificationResponse>> collection = toCollectionModel(results);
        collection.add(linkTo(methodOn(NotificationController.class)
                .getAllNotifications(status, authentication))
                .withSelfRel());
        return ResponseEntity.ok(collection);
    }

    @Operation(summary = "Buscar notificacion por ID", description = "Devuelve una notificacion con enlaces HATEOAS en _links")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notificacion encontrada correctamente"),
            @ApiResponse(responseCode = "400", description = "ID de notificacion invalido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para ver esta notificacion", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontro la notificacion con ese ID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/by-id/{id}")
    @PreAuthorize("hasAnyRole('ADOPTER', 'VOLUNTEER', 'VET', 'SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<EntityModel<NotificationResponse>> getNotificationById(
            @PathVariable Long id,
            Authentication authentication) {

        Optional<NotificationResult> result;

        if (hasRole(authentication, "ROLE_ADMIN")) {
            result = this.service.getByIdIncludingArchived(id);
        } else if (hasRole(authentication, "ROLE_SHELTER_ADMIN")) {
            Long userId = service.getUserIdByEmail(authentication.getName());
            Long shelterId = service.getShelterIdForStaffUser(userId);
            result = this.service.getByIdForUserOrShelter(id, userId, shelterId);
        } else {
            Long userId = service.getUserIdByEmail(authentication.getName());
            result = this.service.getByIdForUser(id, userId);
        }

        return toResponseEntity(result);
    }

    @Operation(summary = "Crear notificacion", description = "Crea una notificacion y devuelve enlaces HATEOAS en _links")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Notificacion creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos de notificacion invalidos, usuario o refugio no valido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para crear notificaciones", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "La notificacion no cumple una regla de negocio", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Servicio remoto no disponible", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<EntityModel<NotificationResponse>> create(
            @Valid @RequestBody NotificationRequest request) {

        NotificationCommand command = toCommand(request);
        NotificationResult result = this.service.create(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(linkAssembler.toModel(toResponse(result)));
    }

    @Operation(summary = "Actualizar notificacion por ID", description = "Actualiza una notificacion y devuelve enlaces HATEOAS en _links")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notificacion actualizada correctamente"),
            @ApiResponse(responseCode = "400", description = "ID o datos de notificacion invalidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para actualizar esta notificacion", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontro la notificacion con ese ID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "La notificacion no cumple una regla de negocio", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Servicio remoto no disponible", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/by-id/{id}")
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<EntityModel<NotificationResponse>> updateNotificationById(
            @PathVariable Long id,
            @Valid @RequestBody NotificationRequest request) {

        NotificationCommand command = toCommand(request);
        return toResponseEntity(this.service.updateById(id, command));
    }

    @DeleteMapping("/by-id/{id}")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Notificacion archivada correctamente"),
            @ApiResponse(responseCode = "400", description = "ID de notificacion invalido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para archivar esta notificacion", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontro la notificacion con ese ID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteNotificationById(
            @PathVariable Long id) {

        if (!this.service.deleteById(id)) {
            return com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
        }

        return ResponseEntity.noContent().build();
    }

    private NotificationCommand toCommand(NotificationRequest request) {
        return new NotificationCommand(
                request.userId(),
                request.shelterId(),
                request.recipient(),
                request.message(),
                request.typeName(),
                request.status()
        );
    }

    private NotificationResponse toResponse(NotificationResult result) {
        return new NotificationResponse(
                result.id(),
                result.userId(),
                result.shelterId(),
                result.recipient(),
                result.message(),
                result.typeId(),
                result.typeName(),
                result.status(),
                result.createdAt()
        );
    }

    private CollectionModel<EntityModel<NotificationResponse>> toCollectionModel(List<NotificationResult> results) {
        List<EntityModel<NotificationResponse>> notifications = results.stream()
                .map(this::toResponse)
                .map(linkAssembler::toModel)
                .toList();
        return CollectionModel.of(notifications);
    }

    private ResponseEntity<EntityModel<NotificationResponse>> toResponseEntity(Optional<NotificationResult> result) {
        return result
                .map(this::toResponse)
                .map(linkAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElse(com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado"));
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }
}





