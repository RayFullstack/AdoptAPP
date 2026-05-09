package com.adoptapp.petservice.controller;

import com.adoptapp.petservice.dto.PetCommand;
import com.adoptapp.petservice.dto.PetRequest;
import com.adoptapp.petservice.dto.PetResponse;
import com.adoptapp.petservice.dto.PetResult;
import com.adoptapp.petservice.service.PetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import com.adoptapp.petservice.dto.ErrorResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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

    @GetMapping("/by-id/{id}")
    public ResponseEntity<PetResponse> getPetById(@PathVariable Long id) {
        return this.service.getById(id)
                .map(result -> toResponse(result))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody PetRequest request) {
        try {
            PetCommand command = toCommand(request);
            PetResult result = this.service.create(command);
            PetResponse response = toResponse(result);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(e.getMessage(),
                            HttpStatus.CONFLICT.value(),
                            LocalDateTime.now()
                    ));
        }
    }

    @PutMapping("/by-id/{id}")
    public ResponseEntity<Object> updatePetById(
            @PathVariable Long id,
            @Valid @RequestBody PetRequest request) {
        try {
            PetCommand command = toCommand(request);
            Optional<PetResult> result = this.service.updateById(id, command);
            if (result.isPresent()) {
                return ResponseEntity.ok(toResponse(result.get()));
            }
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(e.getMessage(),
                    HttpStatus.CONFLICT.value(),
                    LocalDateTime.now()
            ));
        }
    }

    @DeleteMapping("/by-id/{id}")
    public ResponseEntity<Void> deletePetById(@PathVariable Long id) {
        if (!this.service.deleteById(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(new ErrorResponse(message, HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now()
        ));
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
                request.status()
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
                result.fosterId()
        );
    }
}
