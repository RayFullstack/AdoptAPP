package com.adoptapp.petservice.controller;

import com.adoptapp.petservice.dto.PetCommand;
import com.adoptapp.petservice.dto.PetRequest;
import com.adoptapp.petservice.dto.PetResponse;
import com.adoptapp.petservice.dto.PetResult;
import com.adoptapp.petservice.service.PetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.adoptapp.petservice.dto.PetHistoryResult;

import java.util.List;
import java.util.Optional;
@RestController
@RequestMapping("/pets")

public class PetController {
    private final PetService service;

    public PetController(PetService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<PetResponse>> getAllPets(
            @RequestParam(required = false) String status) {

        List<PetResult> results = status != null
                ? this.service.getPets(status)
                : this.service.getPets();
        List<PetResponse> responses = results.stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/by-id/{id}/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PetHistoryResult>> getHistory(@PathVariable Long id) {
        return service.getHistory(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-id/{id}")
    public ResponseEntity<PetResponse> getPetById(@PathVariable Long id) {
        return this.service.getById(id)
                .map(result -> toResponse(result))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADOPTER', 'SHELTER_ADMIN', 'VOLUNTEER', 'ADMIN')")
    public ResponseEntity<PetResponse> create(@Valid @RequestBody PetRequest request) {
        PetCommand command = toCommand(request);
        PetResult result = this.service.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(result));
    }

    @PutMapping("/by-id/{id}")
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'VOLUNTEER', 'ADMIN')")
    public ResponseEntity<PetResponse> updatePetById(
            @PathVariable Long id,
            @Valid @RequestBody PetRequest request) {
        PetCommand command = toCommand(request);
        return this.service.updateById(id, command)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/by-id/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePetById(@PathVariable Long id) {
        boolean deleted = this.service.deleteById(id);
        return deleted
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    private PetCommand toCommand(PetRequest request) {
        return new PetCommand(
                request.name(),
                request.species(),
                request.race(),
                request.age(),
                request.size(),
                request.color(),
                request.personality(),
                request.fosterId(),
                request.vaccinated(),
                request.sterilized(),
                request.diseases(),
                request.status(),
                request.shelterId()
        );
    }

    private PetResponse toResponse(PetResult result) {
        return new PetResponse(
                result.id(),
                result.name(),
                result.species(),
                result.race(),
                result.age(),
                result.size(),
                result.color(),
                result.status(),
                result.vaccinated(),
                result.sterilized(),
                result.diseases(),
                result.personality(),
                result.fosterId(),
                result.shelterId()
        );
    }
}
