package com.adoptapp.followupservice.controller;

<<<<<<< HEAD
import com.adoptapp.followupservice.dto.ErrorResponse;
import com.adoptapp.followupservice.dto.FollowUpCommand;
import com.adoptapp.followupservice.dto.FollowUpRequest;
import com.adoptapp.followupservice.dto.FollowUpResponse;
import com.adoptapp.followupservice.dto.FollowUpResult;
=======
import com.adoptapp.followupservice.dto.*;
>>>>>>> origin/camila-dev
import com.adoptapp.followupservice.service.FollowUpService;
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
>>>>>>> origin/camila-dev

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
<<<<<<< HEAD

=======
>>>>>>> origin/camila-dev
        return this.service.getById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

<<<<<<< HEAD
    @PostMapping
    public ResponseEntity<Object> create(
            @Valid @RequestBody FollowUpRequest request) {

        try {

            FollowUpCommand command = toCommand(request);

            FollowUpResult result = this.service.create(command);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(toResponse(result));

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
    public ResponseEntity<Object> update(
            @PathVariable Long id,
            @Valid @RequestBody FollowUpRequest request) {

        try {

            FollowUpCommand command = toCommand(request);

            Optional<FollowUpResult> result =
                    this.service.updateById(id, command);

            if (result.isPresent()) {

                return ResponseEntity.ok(
                        toResponse(result.get()));
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
    public ResponseEntity<Void> delete(@PathVariable Long id) {

=======
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
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/by-id/{id}")
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
>>>>>>> origin/camila-dev
        if (!this.service.deleteById(id)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

<<<<<<< HEAD
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException e) {

        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity.badRequest()
                .body(new ErrorResponse(
                        message,
                        HttpStatus.BAD_REQUEST.value(),
                        LocalDateTime.now()
                ));
    }

    private FollowUpCommand toCommand(FollowUpRequest request) {

        return new FollowUpCommand(
                request.adopterName(),
                request.petName(),
=======
    private FollowUpCommand toCommand(FollowUpRequest request) {
        return new FollowUpCommand(
                request.adopterName(),
                request.petName(),
                request.userId(),
                request.petId(),
                request.adoptionId(),
>>>>>>> origin/camila-dev
                request.visitDate(),
                request.comments(),
                request.status()
        );
    }

    private FollowUpResponse toResponse(FollowUpResult result) {
<<<<<<< HEAD

=======
>>>>>>> origin/camila-dev
        return new FollowUpResponse(
                result.id(),
                result.adopterName(),
                result.petName(),
<<<<<<< HEAD
                result.visitDate(),
                result.comments(),
                result.status()
        );
    }
}
=======
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
>>>>>>> origin/camila-dev
