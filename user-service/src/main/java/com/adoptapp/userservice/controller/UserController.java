package com.adoptapp.userservice.controller;

import com.adoptapp.userservice.dto.UserCommand;
import com.adoptapp.userservice.dto.UserRequest;
import com.adoptapp.userservice.dto.UserResponse;
import com.adoptapp.userservice.dto.UserResult;
import com.adoptapp.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.adoptapp.userservice.dto.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers(
            @RequestParam(required = false) String status) {

        List<UserResult> results = status != null
                ? this.service.getUsers(status)
                : this.service.getUsers();
        List<UserResponse> responses = results.stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/by-id/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return this.service.getById(id)
                .map(result -> toResponse(result))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody UserRequest request) {
        try {
            UserCommand command = toCommand(request);
            UserResult result = this.service.create(command);
            UserResponse response = toResponse(result);
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
    public ResponseEntity<Object> updateUserById(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request) {
        try {
            UserCommand command = toCommand(request);
            Optional<UserResult> result = this.service.updateById(id, command);
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
    public ResponseEntity<Void> deleteUserById(@PathVariable Long id) {
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

    private UserCommand toCommand(UserRequest request) {
        return new UserCommand(
                request.username(),
                request.name(),
                request.surname(),
                request.email(),
                request.phone(),
                request.address(),
                request.status()

        );
    }

    private UserResponse toResponse(UserResult result) {
        return new UserResponse(
                result.id(),
                result.username(),
                result.name(),
                result.surname(),
                result.email(),
                result.phone(),
                result.address(),
                result.status(),
                result.createdAt()
        );
    }
}

