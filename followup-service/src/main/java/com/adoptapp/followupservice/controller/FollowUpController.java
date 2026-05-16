package com.adoptapp.followupservice.controller;

import com.adoptapp.followupservice.dto.ErrorResponse;
import com.adoptapp.followupservice.dto.FollowUpCommand;
import com.adoptapp.followupservice.dto.FollowUpRequest;
import com.adoptapp.followupservice.dto.FollowUpResponse;
import com.adoptapp.followupservice.dto.FollowUpResult;
import com.adoptapp.followupservice.service.FollowUpService;
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
                .orElse(ResponseEntity.notFound().build());
    }

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

        if (!this.service.deleteById(id)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

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
                result.visitDate(),
                result.comments(),
                result.status()
        );
    }
}