package com.adoptapp.staffservice.controller;

import com.adoptapp.staffservice.dto.*;
import com.adoptapp.staffservice.service.StaffService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/staff")
public class StaffController {

    private final StaffService service;

    public StaffController(StaffService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<StaffResponse>> getAllStaff(
            @RequestParam(required = false) String status) {

        List<StaffResult> results = status != null
                ? this.service.getAllStaff(status)
                : this.service.getAllStaff();

        List<StaffResponse> responses = results.stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/by-id/{id}")
    public ResponseEntity<StaffResponse> getStaffById(@PathVariable Long id) {
        return this.service.getById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-id/{id}/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StaffHistoryResponse>> getHistory(@PathVariable Long id) {
        List<StaffHistoryResponse> history = this.service.getHistory(id);
        return ResponseEntity.ok(history);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<StaffResponse> create(
            @Valid @RequestBody StaffRequest request) {

        StaffCommand command = toCommand(request);
        StaffResult result = this.service.create(command);
        StaffResponse response = toResponse(result);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/by-id/{id}")
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<StaffResponse> updateStaffById(
            @PathVariable Long id,
            @Valid @RequestBody StaffRequest request) {

        StaffCommand command = toCommand(request);

        return this.service.updateById(id, command)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/by-id/{id}")
    @PreAuthorize("hasAnyRole('SHELTER_ADMIN', 'ADMIN')")
    public ResponseEntity<Void> deleteStaffById(@PathVariable Long id) {
        if (!this.service.deleteById(id)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    private StaffCommand toCommand(StaffRequest request) {
        return new StaffCommand(
                request.userId(),
                request.shelterId(),
                request.position(),
                request.phone(),
                request.email(),
                request.hireDate(),
                request.status()
        );
    }

    private StaffResponse toResponse(StaffResult result) {
        return new StaffResponse(
                result.id(),
                result.userId(),
                result.shelterId(),
                result.position(),
                result.phone(),
                result.email(),
                result.hireDate(),
                result.status(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
