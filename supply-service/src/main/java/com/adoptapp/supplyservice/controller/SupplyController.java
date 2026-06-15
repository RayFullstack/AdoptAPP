package com.adoptapp.supplyservice.controller;

import com.adoptapp.supplyservice.dto.*;
import com.adoptapp.supplyservice.service.SupplyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/supplies")
public class SupplyController {

    private final SupplyService supplyService;

    public SupplyController(SupplyService supplyService) {
        this.supplyService = supplyService;
    }

    @GetMapping
    public ResponseEntity<List<SupplyResponse>> getAllSupplies(
            @RequestParam(required = false) String status,
            Authentication authentication) {

        List<SupplyResult> results = getVisibleSupplies(status, authentication);
        List<SupplyResponse> responses = results.stream()
                .map(r -> new SupplyResponse(
                        r.id(), r.name(), r.description(), r.quantity(),
                        r.unit(), r.category(), r.shelterId(),
                        r.supplierName(), r.minimumStock(), r.status(),
                        r.createdAt(), r.updatedAt()
                ))
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/by-id/{id}")
    public ResponseEntity<SupplyResponse> getSupplyById(
            @PathVariable Long id,
            Authentication authentication) {
        return getVisibleSupplyById(id, authentication)
                .map(r -> new SupplyResponse(
                        r.id(), r.name(), r.description(), r.quantity(),
                        r.unit(), r.category(), r.shelterId(),
                        r.supplierName(), r.minimumStock(), r.status(),
                        r.createdAt(), r.updatedAt()
                ))
                .map(ResponseEntity::ok)
                .orElse(com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado"));
    }

    @GetMapping("/shelter/{shelterId}")
    public ResponseEntity<List<SupplyResponse>> getSuppliesByShelter(
            @PathVariable Long shelterId,
            Authentication authentication) {

        if (!canUseShelter(shelterId, authentication)) {
            return com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
        }

        List<SupplyResponse> responses = supplyService.findByShelterId(shelterId).stream()
                .map(r -> new SupplyResponse(
                        r.id(), r.name(), r.description(), r.quantity(),
                        r.unit(), r.category(), r.shelterId(),
                        r.supplierName(), r.minimumStock(), r.status(),
                        r.createdAt(), r.updatedAt()
                ))
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/internal/shelter/{shelterId}/active")
    public ResponseEntity<List<SupplyResponse>> getActiveSuppliesByShelter(@PathVariable Long shelterId) {
        List<SupplyResponse> responses = supplyService.findByShelterId(shelterId).stream()
                .map(r -> new SupplyResponse(
                        r.id(), r.name(), r.description(), r.quantity(),
                        r.unit(), r.category(), r.shelterId(),
                        r.supplierName(), r.minimumStock(), r.status(),
                        r.createdAt(), r.updatedAt()
                ))
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/by-id/{id}/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SupplyHistoryResponse>> getSupplyHistory(@PathVariable Long id) {
        return supplyService.getHistory(id)
                .map(ResponseEntity::ok)
                .orElse(com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SHELTER_ADMIN')")
    public ResponseEntity<SupplyResponse> createSupply(
            @Valid @RequestBody SupplyRequest request,
            Authentication authentication) {

        if (!canUseShelter(request.shelterId(), authentication)) {
            return com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
        }

        SupplyCommand command = new SupplyCommand(
                request.name(), request.description(), request.quantity(),
                request.unit(), request.category(), request.shelterId(),
                request.userId(), request.supplierName(),
                request.minimumStock(), request.status()
        );
        SupplyResult result = supplyService.create(command);
        SupplyResponse response = new SupplyResponse(
                result.id(), result.name(), result.description(), result.quantity(),
                result.unit(), result.category(), result.shelterId(),
                result.supplierName(), result.minimumStock(), result.status(),
                result.createdAt(), result.updatedAt()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/by-id/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHELTER_ADMIN')")
    public ResponseEntity<SupplyResponse> updateSupply(@PathVariable Long id,
                                                       @Valid @RequestBody SupplyRequest request,
                                                       Authentication authentication) {
        if (!canModifySupply(id, authentication) || !canUseShelter(request.shelterId(), authentication)) {
            return com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
        }

        SupplyCommand command = new SupplyCommand(
                request.name(), request.description(), request.quantity(),
                request.unit(), request.category(), request.shelterId(),
                request.userId(), request.supplierName(),
                request.minimumStock(), request.status()
        );
        return supplyService.update(id, command)
                .map(r -> new SupplyResponse(
                        r.id(), r.name(), r.description(), r.quantity(),
                        r.unit(), r.category(), r.shelterId(),
                        r.supplierName(), r.minimumStock(), r.status(),
                        r.createdAt(), r.updatedAt()
                ))
                .map(ResponseEntity::ok)
                .orElse(com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado"));
    }

    @DeleteMapping("/by-id/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHELTER_ADMIN')")
    public ResponseEntity<Void> deleteSupply(
            @PathVariable Long id,
            Authentication authentication) {

        if (!canModifySupply(id, authentication)) {
            return com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
        }

        boolean deleted = supplyService.delete(id);
        return deleted
                ? ResponseEntity.noContent().build()
                : com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
    }

    private List<SupplyResult> getVisibleSupplies(String status, Authentication authentication) {
        if (hasRole(authentication, "ROLE_ADMIN")) {
            return status != null
                    ? supplyService.getSupplies(status)
                    : supplyService.getSupplies();
        }

        if (!hasShelterScopedRole(authentication)) {
            return status != null
                    ? supplyService.getSupplies(status)
                    : supplyService.getSupplies();
        }

        Long shelterId = getShelterIdForAuthenticatedUser(authentication);
        return supplyService.findByShelterId(shelterId, status);
    }

    private Optional<SupplyResult> getVisibleSupplyById(Long id, Authentication authentication) {
        if (hasRole(authentication, "ROLE_ADMIN")) {
            return supplyService.getById(id);
        }

        if (!hasShelterScopedRole(authentication)) {
            return supplyService.getById(id);
        }

        Long shelterId = getShelterIdForAuthenticatedUser(authentication);
        return supplyService.getByIdForShelter(id, shelterId);
    }

    private boolean canModifySupply(Long id, Authentication authentication) {
        return getVisibleSupplyById(id, authentication).isPresent();
    }

    private boolean canUseShelter(Long shelterId, Authentication authentication) {
        if (hasRole(authentication, "ROLE_ADMIN")) {
            return true;
        }

        if (!hasShelterScopedRole(authentication)) {
            return true;
        }

        Long authenticatedShelterId = getShelterIdForAuthenticatedUser(authentication);
        return authenticatedShelterId.equals(shelterId);
    }

    private Long getShelterIdForAuthenticatedUser(Authentication authentication) {
        Long userId = supplyService.getUserIdByEmail(authentication.getName());
        return supplyService.getShelterIdForStaffUser(userId);
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }

    private boolean hasShelterScopedRole(Authentication authentication) {
        return hasRole(authentication, "ROLE_SHELTER_ADMIN")
                || hasRole(authentication, "ROLE_VOLUNTEER")
                || hasRole(authentication, "ROLE_VET");
    }
}
