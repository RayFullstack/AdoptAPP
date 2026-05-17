package com.adoptapp.notificationservice.controller;

import com.adoptapp.notificationservice.dto.*;
import com.adoptapp.notificationservice.service.NotificationService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping
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
    public ResponseEntity<NotificationResponse> getNotificationById(
            @PathVariable Long id) {

        return this.service.getById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
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
    public ResponseEntity<Void> deleteNotificationById(
            @PathVariable Long id) {

        if (!this.service.deleteById(id)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

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
