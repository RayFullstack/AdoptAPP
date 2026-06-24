package com.adoptapp.followupservice.controller;

import com.adoptapp.sharedkernel.dto.ErrorResponse;

import com.adoptapp.followupservice.dto.*;
import com.adoptapp.followupservice.service.FollowUpLinkAssembler;
import com.adoptapp.followupservice.service.FollowUpService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Tag(name = "Seguimientos", description = "Operaciones para gestionar seguimientos post adopcion")
@RestController
@RequestMapping("/followups")
@SecurityRequirement(name = "basicAuth")
public class FollowUpController {

    private final FollowUpService service;
    private final FollowUpLinkAssembler linkAssembler;

    public FollowUpController(FollowUpService service, FollowUpLinkAssembler linkAssembler) {
        this.service = service;
        this.linkAssembler = linkAssembler;
    }

    @Operation(summary = "Listar seguimientos", description = "Devuelve seguimientos con enlaces HATEOAS en _links")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Seguimientos listados correctamente"),
            @ApiResponse(responseCode = "400", description = "Estado de seguimiento invalido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para listar seguimientos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<FollowUpResponse>>> getAll(
            @RequestParam(required = false) String status) {

        List<FollowUpResult> results = status != null
                ? this.service.getFollowUps(status)
                : this.service.getFollowUps();

        CollectionModel<EntityModel<FollowUpResponse>> collection = toCollectionModel(results);
        collection.add(linkTo(methodOn(FollowUpController.class).getAll(status)).withSelfRel());
        return ResponseEntity.ok(collection);
    }

    @Operation(summary = "Buscar seguimiento por ID", description = "Devuelve un seguimiento con enlaces HATEOAS en _links")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Seguimiento encontrado correctamente"),
            @ApiResponse(responseCode = "400", description = "ID de seguimiento invalido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para ver este seguimiento", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontro el seguimiento con ese ID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/by-id/{id}")
    public ResponseEntity<EntityModel<FollowUpResponse>> getById(@PathVariable Long id) {
        return toResponseEntity(this.service.getById(id));
    }

    @GetMapping("/by-id/{id}/history")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historial de seguimiento obtenido correctamente"),
            @ApiResponse(responseCode = "400", description = "ID de seguimiento invalido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para ver el historial de este seguimiento", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontro el seguimiento con ese ID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<FollowUpHistoryResponse>> getHistory(@PathVariable Long id) {
        List<FollowUpHistoryResponse> history = this.service.getHistory(id);
        return ResponseEntity.ok(history);
    }

    @Operation(summary = "Crear seguimiento", description = "Crea un seguimiento y devuelve enlaces HATEOAS en _links")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Seguimiento creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos de seguimiento invalidos, usuario, mascota o adopcion no valida", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para crear seguimientos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "El seguimiento no cumple una regla de negocio", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Servicio remoto no disponible", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<EntityModel<FollowUpResponse>> create(
            @Valid @RequestBody FollowUpRequest request) {

        FollowUpCommand command = toCommand(request);
        FollowUpResult result = this.service.create(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(linkAssembler.toModel(toResponse(result)));
    }

    @Operation(summary = "Actualizar seguimiento por ID", description = "Actualiza un seguimiento y devuelve enlaces HATEOAS en _links")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Seguimiento actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "ID o datos de seguimiento invalidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para actualizar este seguimiento", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontro el seguimiento con ese ID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "El seguimiento no cumple una regla de negocio", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Servicio remoto no disponible", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/by-id/{id}")
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<EntityModel<FollowUpResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody FollowUpRequest request) {

        FollowUpCommand command = toCommand(request);
        return toResponseEntity(this.service.updateById(id, command));
    }

    @DeleteMapping("/by-id/{id}")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Seguimiento eliminado correctamente"),
            @ApiResponse(responseCode = "400", description = "ID de seguimiento invalido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para eliminar este seguimiento", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontro el seguimiento con ese ID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!this.service.deleteById(id)) {
            return com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
        }

        return ResponseEntity.noContent().build();
    }

    private FollowUpCommand toCommand(FollowUpRequest request) {
        return new FollowUpCommand(
                request.adopterName(),
                request.petName(),
                request.userId(),
                request.petId(),
                request.adoptionId(),
                request.visitDate(),
                request.comments(),
                request.status()
        );
    }

    private FollowUpResponse toResponse(FollowUpResult result) {
        return new FollowUpResponse(
                result.id(),
                result.adopterName(),
                result.petName(),
                result.userId(),
                result.petId(),
                result.adoptionId(),
                result.visitDate(),
                result.comments(),
                result.status(),
                result.createdAt(),
                result.updatedAt()
        );
    }

    private CollectionModel<EntityModel<FollowUpResponse>> toCollectionModel(List<FollowUpResult> results) {
        List<EntityModel<FollowUpResponse>> followUps = results.stream()
                .map(this::toResponse)
                .map(linkAssembler::toModel)
                .toList();
        return CollectionModel.of(followUps);
    }

    private ResponseEntity<EntityModel<FollowUpResponse>> toResponseEntity(Optional<FollowUpResult> result) {
        return result
                .map(this::toResponse)
                .map(linkAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElse(com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado"));
    }
}






