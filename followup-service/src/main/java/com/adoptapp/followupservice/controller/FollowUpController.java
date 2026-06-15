package com.adoptapp.followupservice.controller;

import com.adoptapp.followupservice.dto.*;
import com.adoptapp.followupservice.service.FollowUpService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/followups")
public class FollowUpController {

    private final FollowUpService service;

    public FollowUpController(FollowUpService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<FollowUpResponse>> getAll(
            @RequestParam(required = false) String status) {

        List<FollowUpResult> results = status != null
                ? this.service.getFollowUps(status)
                : this.service.getFollowUps();

        List<FollowUpResponse> responses = results.stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/by-id/{id}")
    public ResponseEntity<FollowUpResponse> getById(@PathVariable Long id) {
        return this.service.getById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado"));
    }

    @GetMapping("/by-id/{id}/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<FollowUpHistoryResponse>> getHistory(@PathVariable Long id) {
        List<FollowUpHistoryResponse> history = this.service.getHistory(id);
        return ResponseEntity.ok(history);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<FollowUpResponse> create(
            @Valid @RequestBody FollowUpRequest request) {

        FollowUpCommand command = toCommand(request);
        FollowUpResult result = this.service.create(command);
        FollowUpResponse response = toResponse(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/by-id/{id}")
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<FollowUpResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody FollowUpRequest request) {

        FollowUpCommand command = toCommand(request);

        return this.service.updateById(id, command)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado"));
    }

    @DeleteMapping("/by-id/{id}")
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!this.service.deleteById(id)) {
            return com.adoptapp.sharedkernel.util.ErrorResponseEntity.notFound("Recurso no encontrado");
        }

        return ResponseEntity.noContent().build();
    }

    private FollowUpCommand toCommand(FollowUpRequest request) {
        return new FollowUpCommand(
                request.adopterName(),
                request.petName(),
                request.userId(),
                request.petId(),
                request.adoptionId(),
                request.visitDate(),
                request.comments(),
                request.status()
        );
    }

    private FollowUpResponse toResponse(FollowUpResult result) {
        return new FollowUpResponse(
                result.id(),
                result.adopterName(),
                result.petName(),
                result.userId(),
                result.petId(),
                result.adoptionId(),
                result.visitDate(),
                result.comments(),
                result.status(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
