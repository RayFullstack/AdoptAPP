package com.adoptapp.donationservice.controller;

import com.adoptapp.donationservice.dto.*;
import com.adoptapp.donationservice.service.DonationService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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
                .orElse(com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado"));
    }

    @GetMapping("/by-id/{id}/history")
    public ResponseEntity<List<DonationHistoryResponse>> getHistory(
            @PathVariable Long id) {
        List<DonationHistoryResponse> history = this.service.getHistory(id);
        return ResponseEntity.ok(history);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DonationResponse> create(
            @Valid @RequestBody DonationCreateRequest request) {

        DonationCommand command = toCommand(request);

        DonationResult result = this.service.create(command);
        DonationResponse response = toResponse(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/by-id/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DonationResponse> updateDonationById(
            @PathVariable Long id,
            @Valid @RequestBody DonationUpdateRequest request) {

        DonationCommand command = toCommand(request);

        return this.service.updateById(id, command)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado"));
    }

    @DeleteMapping("/by-id/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDonationById(
            @PathVariable Long id) {

        if (!this.service.deleteById(id)) {
            return com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
        }

        return ResponseEntity.noContent().build();
    }

    private DonationCommand toCommand(DonationCreateRequest request) {
        return new DonationCommand(
                request.donorName(),
                request.amount(),
                request.description(),
                null,
                request.userId(),
                request.shelterId()
        );
    }

    private DonationCommand toCommand(DonationUpdateRequest request) {
        return new DonationCommand(
                request.donorName(),
                request.amount(),
                request.description(),
                request.status(),
                request.userId(),
                request.shelterId()
        );
    }

    private DonationResponse toResponse(DonationResult result) {

        return new DonationResponse(
                result.id(),
                result.donorName(),
                result.amount(),
                result.description(),
                result.status(),
                result.userId(),
                result.shelterId(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}