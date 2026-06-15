package com.adoptapp.notificationservice.controller;

import com.adoptapp.notificationservice.dto.*;
import com.adoptapp.notificationservice.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADOPTER', 'VOLUNTEER', 'VET', 'SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<List<NotificationResponse>> getAllNotifications(
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

        List<NotificationResponse> responses = results.stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/by-id/{id}")
    @PreAuthorize("hasAnyRole('ADOPTER', 'VOLUNTEER', 'VET', 'SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<NotificationResponse> getNotificationById(
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

        return result
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado"));
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<NotificationResponse> create(
            @Valid @RequestBody NotificationRequest request) {

        NotificationCommand command = toCommand(request);
        NotificationResult result = this.service.create(command);
        NotificationResponse response = toResponse(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/by-id/{id}")
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<NotificationResponse> updateNotificationById(
            @PathVariable Long id,
            @Valid @RequestBody NotificationRequest request) {

        NotificationCommand command = toCommand(request);

        return this.service.updateById(id, command)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado"));
    }

    @DeleteMapping("/by-id/{id}")
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
}
