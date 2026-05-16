package com.adoptapp.adoptionservice.controller;

import com.adoptapp.adoptionservice.dto.AdoptionCommand;
import com.adoptapp.adoptionservice.dto.AdoptionHistoryResponse;
import com.adoptapp.adoptionservice.dto.AdoptionRequest;
import com.adoptapp.adoptionservice.dto.AdoptionResponse;
import com.adoptapp.adoptionservice.dto.AdoptionResult;
import com.adoptapp.adoptionservice.model.Adoption;
import com.adoptapp.adoptionservice.service.AdoptionService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/adoptions")
public class AdoptionController {

    private final AdoptionService service;

    public AdoptionController(AdoptionService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<AdoptionResponse>> getAllAdoptions(
            @RequestParam(required = false) String status) {

        List<AdoptionResult> results = status != null
                ? this.service.getAdoptions(status)
                : this.service.getAdoptions();

        List<AdoptionResponse> responses = results.stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/by-id/{id}")
    public ResponseEntity<AdoptionResponse> getAdoptionById(@PathVariable Long id) {

        return this.service.getById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<AdoptionHistoryResponse>> getHistory(@PathVariable Long id) {
        List<AdoptionHistoryResponse> history = this.service.getHistory(id);
        return ResponseEntity.ok(history);
    }

    @PostMapping
    public ResponseEntity<AdoptionResponse> createAdoption(
            @Valid @RequestBody AdoptionRequest request) {

        AdoptionCommand command = toCommand(request);
        AdoptionResult result = this.service.create(command);
        AdoptionResponse response = toResponse(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdoptionResponse> updateAdoptionById(
            @PathVariable Long id,
            @Valid @RequestBody AdoptionRequest request) {

        AdoptionCommand command = toCommand(request);

        return this.service.updateById(id, command)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdoptionById(
            @PathVariable Long id) {

        service.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    private AdoptionCommand toCommand(AdoptionRequest request) {

        return new AdoptionCommand(
                request.userId(),
                request.petId(),
                request.status()
        );
    }

    private AdoptionResponse toResponse(AdoptionResult result) {

        return new AdoptionResponse(
                result.id(),
                result.userId(),
                result.petId(),
                result.status(),
                result.createdAt()
        );
    }
}