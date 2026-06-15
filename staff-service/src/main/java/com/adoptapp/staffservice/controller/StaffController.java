package com.adoptapp.staffservice.controller;

import com.adoptapp.staffservice.dto.*;
import com.adoptapp.staffservice.service.StaffService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/staff")
public class StaffController {

    private final StaffService service;

    public StaffController(StaffService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<StaffResponse>> getAllStaff(
            @RequestParam(required = false) String status,
            Authentication authentication) {

        List<StaffResult> results = getVisibleStaff(status, authentication);

        List<StaffResponse> responses = results.stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/by-id/{id}")
    public ResponseEntity<StaffResponse> getStaffById(
            @PathVariable Long id,
            Authentication authentication) {

        return getVisibleStaffById(id, authentication)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado"));
    }

    @GetMapping("/by-user/{userId}")
    public ResponseEntity<StaffResponse> getStaffByUserId(@PathVariable Long userId) {
        return this.service.getByUserId(userId)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado"));
    }

    @GetMapping("/internal/shelter/{shelterId}/active")
    public ResponseEntity<List<StaffResponse>> getActiveStaffByShelter(@PathVariable Long shelterId) {
        return ResponseEntity.ok(
                this.service.getAllStaffByShelter(shelterId).stream()
                        .map(this::toResponse)
                        .toList()
        );
    }

    @GetMapping("/by-id/{id}/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StaffHistoryResponse>> getHistory(@PathVariable Long id) {
        List<StaffHistoryResponse> history = this.service.getHistory(id);
        return ResponseEntity.ok(history);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<StaffResponse> create(
            @Valid @RequestBody StaffRequest request,
            Authentication authentication) {

        if (!canUseShelter(request.shelterId(), authentication)) {
            return com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
        }

        StaffCommand command = toCommand(request);
        StaffResult result = this.service.create(command);
        StaffResponse response = toResponse(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/by-id/{id}")
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<StaffResponse> updateStaffById(
            @PathVariable Long id,
            @Valid @RequestBody StaffRequest request,
            Authentication authentication) {

        if (!canModifyStaff(id, authentication) || !canUseShelter(request.shelterId(), authentication)) {
            return com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
        }

        StaffCommand command = toCommand(request);

        return this.service.updateById(id, command)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado"));
    }

    @DeleteMapping("/by-id/{id}")
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
