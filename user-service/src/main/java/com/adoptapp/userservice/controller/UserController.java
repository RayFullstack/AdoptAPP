package com.adoptapp.userservice.controller;

import com.adoptapp.sharedkernel.dto.UserAuthResponse;
import com.adoptapp.userservice.dto.*;
import com.adoptapp.userservice.model.UserStatus;
import com.adoptapp.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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

    @GetMapping("/by-email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        return this.service.getByEmail(email)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-email/{email}/auth")
    public ResponseEntity<UserAuthResponse> getUserAuthByEmail(@PathVariable String email) {
        return this.service.getAuthByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-id/{id}/history")
    public ResponseEntity<List<UserHistoryResult>> getHistory(@PathVariable Long id) {
        return service.getHistory(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADOPTER', 'SHELTER_ADMIN', 'VOLUNTEER', 'VET', 'ADMIN')")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        UserCommand command = toCommand(request);
        UserResult result = this.service.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(result));
    }

    @PutMapping("/by-id/{id}")
    @PreAuthorize("@userSecurity.canEdit(#id, authentication)")
    public ResponseEntity<UserResponse> updateUserById(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request) {
        UserCommand command = toCommand(request);
        return this.service.updateById(id, command)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/by-id/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUserById(@PathVariable Long id) {
        if (!this.service.deleteById(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    private UserCommand toCommand(UserRequest request) {
        return new UserCommand(
                request.username(),
                request.name(),
                request.surname(),
                request.email(),
                request.password(),
                request.phone(),
                request.country(),
                request.city(),
                request.street(),
                request.homeNumber(),
                request.postalCode(),
                request.type(),
                request.status(),
                request.role(),
                request.active()
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
                result.country(),
                result.city(),
                result.street(),
                result.homeNumber(),
                result.postalCode(),
                result.type(),
                result.status(),
                result.role(),
                result.active()
        );
    }
}

