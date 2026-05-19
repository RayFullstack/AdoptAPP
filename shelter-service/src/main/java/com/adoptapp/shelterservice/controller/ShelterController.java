package com.adoptapp.shelterservice.controller;

import com.adoptapp.shelterservice.dto.*;
import com.adoptapp.shelterservice.service.ShelterService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
                .orElse(ResponseEntity.notFound().build());
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
            @RequestParam Long userId) {

        ShelterCommand command = toCommand(request);

        ShelterResult result = this.service.create(command, userId);
        ShelterResponse response = toResponse(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/by-id/{id}")
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<ShelterResponse> updateShelterById(
            @PathVariable Long id,
            @Valid @RequestBody ShelterRequest request,
            @RequestParam Long userId) {

        ShelterCommand command = toCommand(request);

        return this.service.updateById(id, command, userId)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/by-id/{id}")
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<Void> deleteShelterById(
            @PathVariable Long id,
            @RequestParam Long userId) {

        if (!this.service.deleteById(id, userId)) {
            return ResponseEntity.notFound().build();
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
}
