package com.adoptapp.healthservice.controller;

import com.adoptapp.healthservice.dto.*;
import com.adoptapp.healthservice.service.HealthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/health")
public class HealthController {

    private final HealthService service;

    public HealthController(HealthService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<HealthResponse>> getAllHealth(
            @RequestParam(required = false) String vaccinationStatus,
            @RequestParam(required = false) String sterilizationStatus) {

        List<HealthResult> results;
        if(vaccinationStatus != null ) {
            results = this.service.getVax(vaccinationStatus);
        } else if (sterilizationStatus != null) {
            results = this.service.getSter(sterilizationStatus);
        } else {
             results = this.service.getHealth();
        }

        List<HealthResponse> responses = results.stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/by-id/{id}")
    public ResponseEntity<HealthResponse> getHealthById(@PathVariable Long id) {

        return this.service.getById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado"));
    }

    @GetMapping("/by-pet/{petId}")
    public ResponseEntity<HealthResponse> getHealthByPetId(@PathVariable Long petId) {

        return this.service.getByPetId(petId)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado"));
    }

    @GetMapping("/by-id/{id}/history")
    public ResponseEntity<List<HealthHistoryResponse>> getHistory(@PathVariable Long id) {
        List<HealthHistoryResponse> history = this.service.getHistory(id);
        return ResponseEntity.ok(history);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('VET', 'SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<HealthResponse> createForm(
            @Valid @RequestBody HealthRequest request,
            Authentication authentication) {

        if (!canUsePet(request.petId(), authentication)) {
            return com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
        }

        HealthCommand command = toCommand(request);
        HealthResult result = this.service.create(command);
        HealthResponse response = toResponse(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/by-id/{id}")
    @PreAuthorize("hasAnyRole('VET', 'SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<HealthResponse> updateById(
            @PathVariable Long id,
            @Valid @RequestBody HealthRequest request,
            Authentication authentication) {

        if (!canModifyHealth(id, authentication) || !canUsePet(request.petId(), authentication)) {
            return com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
        }

        HealthCommand command = toCommand(request);

        return this.service.updateById(id, command)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado"));
    }

    @DeleteMapping("/by-id/{id}")
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
