package com.adoptapp.donationservice.controller;

import com.adoptapp.sharedkernel.dto.ErrorResponse;

import com.adoptapp.donationservice.dto.*;
import com.adoptapp.donationservice.service.DonationLinkAssembler;
import com.adoptapp.donationservice.service.DonationService;
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

@Tag(name = "Donaciones", description = "Operaciones para gestionar donaciones")
@RestController
@RequestMapping("/donations")
@SecurityRequirement(name = "basicAuth")
public class DonationController {

    private final DonationService service;
    private final DonationLinkAssembler linkAssembler;

    public DonationController(DonationService service, DonationLinkAssembler linkAssembler) {
        this.service = service;
        this.linkAssembler = linkAssembler;
    }

    @Operation(summary = "Listar donaciones", description = "Devuelve donaciones con enlaces HATEOAS en _links")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Donaciones listadas correctamente"),
            @ApiResponse(responseCode = "400", description = "Estado de donacion invalido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para listar donaciones", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<DonationResponse>>> getAllDonations(
            @RequestParam(required = false) String status) {

        List<DonationResult> results = status != null
                ? this.service.getDonations(status)
                : this.service.getDonations();

        CollectionModel<EntityModel<DonationResponse>> collection = toCollectionModel(results);
        collection.add(linkTo(methodOn(DonationController.class).getAllDonations(status)).withSelfRel());
        return ResponseEntity.ok(collection);
    }

    @Operation(summary = "Buscar donacion por ID", description = "Devuelve una donacion con enlaces HATEOAS en _links")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Donacion encontrada correctamente"),
            @ApiResponse(responseCode = "400", description = "ID de donacion invalido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para ver esta donacion", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontro la donacion con ese ID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/by-id/{id}")
    public ResponseEntity<EntityModel<DonationResponse>> getDonationById(
            @PathVariable Long id) {

        return toResponseEntity(this.service.getById(id));
    }

    @GetMapping("/by-id/{id}/history")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historial de donacion obtenido correctamente"),
            @ApiResponse(responseCode = "400", description = "ID de donacion invalido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para ver el historial", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontro la donacion con ese ID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<DonationHistoryResponse>> getHistory(
            @PathVariable Long id) {
        List<DonationHistoryResponse> history = this.service.getHistory(id);
        return ResponseEntity.ok(history);
    }

    @Operation(summary = "Crear donacion", description = "Crea una donacion y devuelve enlaces HATEOAS en _links")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Donacion creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos de donacion invalidos, usuario o refugio no valido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para crear donaciones", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "La donacion no cumple una regla de negocio", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Servicio remoto no disponible", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EntityModel<DonationResponse>> create(
            @Valid @RequestBody DonationCreateRequest request) {

        DonationCommand command = toCommand(request);
        DonationResult result = this.service.create(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(linkAssembler.toModel(toResponse(result)));
    }

    @Operation(summary = "Actualizar donacion por ID", description = "Actualiza una donacion y devuelve enlaces HATEOAS en _links")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Donacion actualizada correctamente"),
            @ApiResponse(responseCode = "400", description = "ID o datos de donacion invalidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para actualizar esta donacion", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontro la donacion con ese ID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "La donacion no cumple una regla de negocio", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Servicio remoto no disponible", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/by-id/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EntityModel<DonationResponse>> updateDonationById(
            @PathVariable Long id,
            @Valid @RequestBody DonationUpdateRequest request) {

        DonationCommand command = toCommand(request);
        return toResponseEntity(this.service.updateById(id, command));
    }

    @DeleteMapping("/by-id/{id}")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Donacion eliminada correctamente"),
            @ApiResponse(responseCode = "400", description = "ID de donacion invalido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para eliminar esta donacion", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No se encontro la donacion con ese ID", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDonationById(
            @PathVariable Long id) {

        if (!this.service.deleteById(id)) {
            return com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
        }

        return ResponseEntity.noContent().build();
    }

    private DonationCommand toCommand(DonationCreateRequest request) {
        return new DonationCommand(
                request.donorName(),
                request.amount(),
                request.description(),
                null,
                request.userId(),
                request.shelterId()
        );
    }

    private DonationCommand toCommand(DonationUpdateRequest request) {
        return new DonationCommand(
                request.donorName(),
                request.amount(),
                request.description(),
                request.status(),
                request.userId(),
                request.shelterId()
        );
    }

    private DonationResponse toResponse(DonationResult result) {
        return new DonationResponse(
                result.id(),
                result.donorName(),
                result.amount(),
                result.description(),
                result.status(),
                result.userId(),
                result.shelterId(),
                result.createdAt(),
                result.updatedAt()
        );
    }

    private CollectionModel<EntityModel<DonationResponse>> toCollectionModel(List<DonationResult> results) {
        List<EntityModel<DonationResponse>> donations = results.stream()
                .map(this::toResponse)
                .map(linkAssembler::toModel)
                .toList();
        return CollectionModel.of(donations);
    }

    private ResponseEntity<EntityModel<DonationResponse>> toResponseEntity(Optional<DonationResult> result) {
        return result
                .map(this::toResponse)
                .map(linkAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElse(com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado"));
    }
}






