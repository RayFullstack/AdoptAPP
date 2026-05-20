package com.adoptapp.notificationservice.controller;

import com.adoptapp.notificationservice.dto.*;
import com.adoptapp.notificationservice.service.NotificationService;
<<<<<<< HEAD

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
=======
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
>>>>>>> origin/camila-dev

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping
<<<<<<< HEAD
=======
    @PreAuthorize("hasAnyRole('ADOPTER', 'VOLUNTEER', 'VET', 'SHELTER_ADMIN', 'ADMIN')")
>>>>>>> origin/camila-dev
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
<<<<<<< HEAD
=======
    @PreAuthorize("hasAnyRole('ADOPTER', 'VOLUNTEER', 'VET', 'SHELTER_ADMIN', 'ADMIN')")
>>>>>>> origin/camila-dev
    public ResponseEntity<NotificationResponse> getNotificationById(
            @PathVariable Long id) {

        return this.service.getById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
<<<<<<< HEAD
    public ResponseEntity<Object> create(
            @Valid @RequestBody NotificationRequest request) {

        try {

            NotificationCommand command = toCommand(request);

            NotificationResult result = this.service.create(command);

            NotificationResponse response = toResponse(result);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(response);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(
                            e.getMessage(),
                            HttpStatus.CONFLICT.value(),
                            LocalDateTime.now()
                    ));
        }
    }

    @PutMapping("/by-id/{id}")
    public ResponseEntity<Object> updateNotificationById(
            @PathVariable Long id,
            @Valid @RequestBody NotificationRequest request) {

        try {

            NotificationCommand command = toCommand(request);

            Optional<NotificationResult> result =
                    this.service.updateById(id, command);

            if (result.isPresent()) {
                return ResponseEntity.ok(
                        toResponse(result.get())
                );
            }

            return ResponseEntity.notFound().build();

        } catch (IllegalArgumentException e) {

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(
                            e.getMessage(),
                            HttpStatus.CONFLICT.value(),
                            LocalDateTime.now()
                    ));
        }
    }

    @DeleteMapping("/by-id/{id}")
=======
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
>>>>>>> origin/camila-dev
    public ResponseEntity<Void> deleteNotificationById(
            @PathVariable Long id) {

        if (!this.service.deleteById(id)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

<<<<<<< HEAD
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException e) {

        String message = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity.badRequest()
                .body(new ErrorResponse(
                        message,
                        HttpStatus.BAD_REQUEST.value(),
                        LocalDateTime.now()
                ));
    }

    private NotificationCommand toCommand(NotificationRequest request) {

        return new NotificationCommand(
                request.recipient(),
                request.message(),
                request.type(),
=======
    private NotificationCommand toCommand(NotificationRequest request) {
        return new NotificationCommand(
                request.userId(),
                request.recipient(),
                request.message(),
                request.typeName(),
>>>>>>> origin/camila-dev
                request.status()
        );
    }

    private NotificationResponse toResponse(NotificationResult result) {
<<<<<<< HEAD

        return new NotificationResponse(
                result.id(),
                result.recipient(),
                result.message(),
                result.type(),
                result.status()
=======
        return new NotificationResponse(
                result.id(),
                result.userId(),
                result.recipient(),
                result.message(),
                result.typeId(),
                result.typeName(),
                result.status(),
                result.createdAt()
>>>>>>> origin/camila-dev
        );
    }
}
