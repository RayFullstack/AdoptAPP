package com.adoptapp.donationservice.controller;

import com.adoptapp.donationservice.dto.*;
import com.adoptapp.donationservice.service.DonationService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
<<<<<<< HEAD
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
=======
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
>>>>>>> origin/camila-dev

@RestController
@RequestMapping("/donations")
public class DonationController {

    private final DonationService service;

    public DonationController(DonationService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<DonationResponse>> getAllDonations(
            @RequestParam(required = false) String status) {

        List<DonationResult> results = status != null
                ? this.service.getDonations(status)
                : this.service.getDonations();

        List<DonationResponse> responses = results.stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/by-id/{id}")
    public ResponseEntity<DonationResponse> getDonationById(
            @PathVariable Long id) {

        return this.service.getById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

<<<<<<< HEAD
    @PostMapping
    public ResponseEntity<Object> create(
            @Valid @RequestBody DonationRequest request) {

        try {

            DonationCommand command = toCommand(request);

            DonationResult result = this.service.create(command);

            DonationResponse response = toResponse(result);

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
    public ResponseEntity<Object> updateDonationById(
            @PathVariable Long id,
            @Valid @RequestBody DonationRequest request) {

        try {

            DonationCommand command = toCommand(request);

            Optional<DonationResult> result =
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
    @GetMapping("/by-id/{id}/history")
    public ResponseEntity<List<DonationHistoryResponse>> getHistory(
            @PathVariable Long id) {
        List<DonationHistoryResponse> history = this.service.getHistory(id);
        return ResponseEntity.ok(history);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DonationResponse> create(
            @Valid @RequestBody DonationRequest request) {

        DonationCommand command = toCommand(request);

        DonationResult result = this.service.create(command);
        DonationResponse response = toResponse(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/by-id/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DonationResponse> updateDonationById(
            @PathVariable Long id,
            @Valid @RequestBody DonationRequest request) {

        DonationCommand command = toCommand(request);

        return this.service.updateById(id, command)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/by-id/{id}")
    @PreAuthorize("hasRole('ADMIN')")
>>>>>>> origin/camila-dev
    public ResponseEntity<Void> deleteDonationById(
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

=======
>>>>>>> origin/camila-dev
    private DonationCommand toCommand(DonationRequest request) {

        return new DonationCommand(
                request.donorName(),
                request.amount(),
                request.description(),
<<<<<<< HEAD
                request.status()
=======
                request.status(),
                request.userId(),
                request.shelterId()
>>>>>>> origin/camila-dev
        );
    }

    private DonationResponse toResponse(DonationResult result) {

        return new DonationResponse(
                result.id(),
                result.donorName(),
                result.amount(),
                result.description(),
<<<<<<< HEAD
                result.status()
=======
                result.status(),
                result.userId(),
                result.shelterId(),
                result.createdAt(),
                result.updatedAt()
>>>>>>> origin/camila-dev
        );
    }
}