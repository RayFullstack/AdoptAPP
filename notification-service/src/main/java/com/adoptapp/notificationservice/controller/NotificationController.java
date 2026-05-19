package com.adoptapp.notificationservice.controller;

import com.adoptapp.notificationservice.dto.*;
import com.adoptapp.notificationservice.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
            @RequestParam(required = false) String status) {

        List<NotificationResult> results = status != null
                ? this.service.getNotifications(status)
                : this.service.getNotifications();

        List<NotificationResponse> responses = results.stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/by-id/{id}")
    @PreAuthorize("hasAnyRole('ADOPTER', 'VOLUNTEER', 'VET', 'SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<NotificationResponse> getNotificationById(
            @PathVariable Long id) {

        return this.service.getById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
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
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/by-id/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteNotificationById(
            @PathVariable Long id) {

        if (!this.service.deleteById(id)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    private NotificationCommand toCommand(NotificationRequest request) {
        return new NotificationCommand(
                request.userId(),
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
                result.recipient(),
                result.message(),
                result.typeId(),
                result.typeName(),
                result.status(),
                result.createdAt()
        );
    }
}
