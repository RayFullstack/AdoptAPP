package com.adoptapp.shelterservice.controller;

import com.adoptapp.shelterservice.dto.*;
import com.adoptapp.shelterservice.service.ShelterService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shelters")
public class ShelterController {

    private final ShelterService service;

    public ShelterController(ShelterService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ShelterResponse>> getAllShelters(
            @RequestParam(required = false) String status) {

        List<ShelterResult> results = status != null
                ? this.service.getShelters(status)
                : this.service.getShelters();

        List<ShelterResponse> responses = results.stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/by-id/{id}")
    public ResponseEntity<ShelterResponse> getShelterById(
            @PathVariable Long id) {

        return this.service.getById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado"));
    }

    @GetMapping("/by-id/{id}/history")
    public ResponseEntity<List<ShelterHistoryResponse>> getHistory(
            @PathVariable Long id) {
        List<ShelterHistoryResponse> history = this.service.getHistory(id);
        return ResponseEntity.ok(history);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<ShelterResponse> create(
            @Valid @RequestBody ShelterRequest request,
            Authentication authentication) {

        ShelterCommand command = toCommand(request);
        Long userId = getAuthenticatedUserId(authentication);

        ShelterResult result = this.service.create(command, userId);
        ShelterResponse response = toResponse(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/by-id/{id}")
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<ShelterResponse> updateShelterById(
            @PathVariable Long id,
            @Valid @RequestBody ShelterRequest request,
            Authentication authentication) {

        if (!canModifyShelter(id, authentication)) {
            return com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
        }

        ShelterCommand command = toCommand(request);
        Long userId = getAuthenticatedUserId(authentication);

        return this.service.updateById(id, command, userId)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado"));
    }

    @DeleteMapping("/by-id/{id}")
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<Void> deleteShelterById(
            @PathVariable Long id,
            Authentication authentication) {

        if (!canModifyShelter(id, authentication)) {
            return com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
        }

        Long userId = getAuthenticatedUserId(authentication);
        if (!this.service.deleteById(id, userId)) {
            return com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
        }

        return ResponseEntity.noContent().build();
    }

    private ShelterCommand toCommand(ShelterRequest request) {
        return new ShelterCommand(
                request.name(),
                request.email(),
                request.phone(),
                request.description(),
                request.status()
        );
    }

    private ShelterResponse toResponse(ShelterResult result) {
        return new ShelterResponse(
                result.id(),
                result.name(),
                result.email(),
                result.phone(),
                result.description(),
                result.status(),
                result.active(),
                result.createdAt(),
                result.updatedAt()
        );
    }

    private boolean canModifyShelter(Long shelterId, Authentication authentication) {
        if (hasRole(authentication, "ROLE_ADMIN")) {
            return true;
        }

        Long authenticatedShelterId = getShelterIdForAuthenticatedUser(authentication);
        return authenticatedShelterId.equals(shelterId)
                && this.service.getByIdActive(shelterId).isPresent();
    }

    private Long getAuthenticatedUserId(Authentication authentication) {
        return service.getUserIdByEmail(authentication.getName());
    }

    private Long getShelterIdForAuthenticatedUser(Authentication authentication) {
        Long userId = getAuthenticatedUserId(authentication);
        return service.getShelterIdForStaffUser(userId);
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }
}
