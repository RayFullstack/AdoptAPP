package com.adoptapp.healthservice.controller;

import com.adoptapp.healthservice.dto.*;
import com.adoptapp.healthservice.service.HealthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/health")
public class HealthController {
    private final HealthService service;

    public HealthController(HealthService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<HealthResponse>> getAllHealth(
            @RequestParam(required = false) String vaccinationStatus,
            @RequestParam(required = false) String sterilizationStatus) {

        List<HealthResult> results;
        if(vaccinationStatus != null ) {
            results = this.service.getVax(vaccinationStatus);
        } else if (sterilizationStatus != null) {
            results = this.service.getSter(sterilizationStatus);
        } else {
             results = this.service.getHealth();
        }

        List<HealthResponse> responses = results.stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/by-id/{id}")
    public ResponseEntity<HealthResponse> getHealthById(@PathVariable Long id) {

        return this.service.getById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<HealthHistoryResponse>> getHistory(@PathVariable Long id) {
        List<HealthHistoryResponse> history = this.service.getHistory(id);
        return ResponseEntity.ok(history);
    }

    @PostMapping
    public ResponseEntity<HealthResponse> createForm(
            @Valid @RequestBody HealthRequest request) {

        HealthCommand command = toCommand(request);
        HealthResult result = this.service.create(command);
        HealthResponse response = toResponse(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HealthResponse> updateFormById(
            @PathVariable Long id,
            @Valid @RequestBody HealthRequest request) {

        HealthCommand command = toCommand(request);

        return this.service.updateById(id, command)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFormById(
            @PathVariable Long id) {

        service.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    private HealthCommand toCommand(HealthRequest request) {

        return new HealthCommand(
                request.userId(),
                request.petId(),
                request.vaccinationStatus(),
                request.sterilizationStatus(),
                request.diseases()
        );
    }

    private HealthResponse toResponse(HealthResult result) {

        return new HealthResponse(
                result.id(),
                result.userId(),
                result.petId(),
                result.vaccinationStatus(),
                result.sterilizationStatus(),
                result.diseases(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
