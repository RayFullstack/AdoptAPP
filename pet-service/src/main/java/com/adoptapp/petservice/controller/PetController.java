package com.adoptapp.petservice.controller;

import com.adoptapp.petservice.dto.*;
import com.adoptapp.petservice.service.PetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/pets")

public class PetController {
    private final PetService service;

    public PetController(PetService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<PetResponse>> getAllPets() {
        return ResponseEntity.ok(toResponseList(this.service.getPets()));
    }

    @GetMapping("/internal/shelter/{shelterId}/active")
    public ResponseEntity<List<PetResponse>> getActivePetsByShelter(@PathVariable Long shelterId) {
        return ResponseEntity.ok(toResponseList(this.service.getPetsByShelter(shelterId)));
    }


    @GetMapping("/by-id/{id}/health")
    public ResponseEntity<com.adoptapp.petservice.dto.HealthResult> getPetHealth(@PathVariable Long id) {
        return service.getHealthInfo(id)
                .map(ResponseEntity::ok)
                .orElse(com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado"));
    }

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


    @GetMapping("/by-id/{id}")
    public ResponseEntity<PetResponse> getPetById(@PathVariable Long id) {
        return toResponseEntity(this.service.getById(id));
    }


    @GetMapping("/admin/by-id/{id}")
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<PetResponse> getByIdIncludingDeleted(
            @PathVariable Long id,
            Authentication authentication) {

        if (isAdmin(authentication)) {
            return toResponseEntity(this.service.getByIdIncludingDeleted(id));
        }

        Long shelterId = getShelterIdForAuthenticatedUser(authentication);
        return toResponseEntity(this.service.getByIdIncludingDeletedForShelter(id, shelterId));
    }


    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<List<PetResponse>> getPetsByStatusAdmin(
            @RequestParam(required = false) String status,
            Authentication authentication) {

        List<PetResult> results = getVisibleAdminPets(status, authentication);

        return ResponseEntity.ok(toResponseList(results));
    }


    @PostMapping
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'VOLUNTEER', 'ADMIN')")
    public ResponseEntity<PetResponse> create(
            @Valid @RequestBody PetRequest request,
            Authentication authentication) {
        PetCommand command = toCommand(request);
        Long shelterId = isAdmin(authentication)
                ? null
                : getShelterIdForAuthenticatedUser(authentication);

        PetResult result = isAdmin(authentication)
                ? this.service.create(command)
                : this.service.createForShelter(command, shelterId);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(result));
    }

    @PutMapping("/by-id/{id}")
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'VOLUNTEER', 'ADMIN')")
    public ResponseEntity<PetResponse> updatePetById(
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

    @PatchMapping("/by-id/{id}/status")
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'VOLUNTEER', 'ADMIN')")
    public ResponseEntity<PetResponse> updatePetByStatus(@PathVariable Long id,
                                                    @Valid @RequestBody PetStatusRequest request,
                                                    Authentication authentication) {
        if (!canModifyPet(id, null, authentication)) {
            return com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
        }

        return toResponseEntity(this.service.updateByStatus(id, request.status()));
    }

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

    private List<PetResponse> toResponseList(List<PetResult> results) {
        return results.stream()
                .map(this::toResponse)
                .toList();
    }

    private ResponseEntity<PetResponse> toResponseEntity(Optional<PetResult> result) {
        return result
                .map(this::toResponse)
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
