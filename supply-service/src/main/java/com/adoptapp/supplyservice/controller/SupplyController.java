package com.adoptapp.supplyservice.controller;

import com.adoptapp.supplyservice.dto.*;
import com.adoptapp.supplyservice.service.SupplyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/supplies")
public class SupplyController {

    private final SupplyService supplyService;

    public SupplyController(SupplyService supplyService) {
        this.supplyService = supplyService;
    }

    @GetMapping
    public ResponseEntity<List<SupplyResponse>> getAllSupplies(
            @RequestParam(required = false) String status) {

        List<SupplyResult> results = status != null
                ? supplyService.getSupplies(status)
                : supplyService.getSupplies();
        List<SupplyResponse> responses = results.stream()
                .map(r -> new SupplyResponse(
                        r.id(), r.name(), r.description(), r.quantity(),
                        r.unit(), r.category(), r.shelterId(),
                        r.supplierName(), r.minimumStock(), r.status(),
                        r.createdAt(), r.updatedAt()
                ))
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/by-id/{id}")
    public ResponseEntity<SupplyResponse> getSupplyById(@PathVariable Long id) {
        return supplyService.getById(id)
                .map(r -> new SupplyResponse(
                        r.id(), r.name(), r.description(), r.quantity(),
                        r.unit(), r.category(), r.shelterId(),
                        r.supplierName(), r.minimumStock(), r.status(),
                        r.createdAt(), r.updatedAt()
                ))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/shelter/{shelterId}")
    public ResponseEntity<List<SupplyResponse>> getSuppliesByShelter(@PathVariable Long shelterId) {
        List<SupplyResponse> responses = supplyService.findByShelterId(shelterId).stream()
                .map(r -> new SupplyResponse(
                        r.id(), r.name(), r.description(), r.quantity(),
                        r.unit(), r.category(), r.shelterId(),
                        r.supplierName(), r.minimumStock(), r.status(),
                        r.createdAt(), r.updatedAt()
                ))
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/by-id/{id}/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SupplyHistoryResponse>> getSupplyHistory(@PathVariable Long id) {
        return supplyService.getHistory(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SHELTER_ADMIN')")
    public ResponseEntity<SupplyResponse> createSupply(@Valid @RequestBody SupplyRequest request) {
        SupplyCommand command = new SupplyCommand(
                request.name(), request.description(), request.quantity(),
                request.unit(), request.category(), request.shelterId(),
                request.userId(), request.supplierName(),
                request.minimumStock(), request.status()
        );
        SupplyResult result = supplyService.create(command);
        SupplyResponse response = new SupplyResponse(
                result.id(), result.name(), result.description(), result.quantity(),
                result.unit(), result.category(), result.shelterId(),
                result.supplierName(), result.minimumStock(), result.status(),
                result.createdAt(), result.updatedAt()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/by-id/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHELTER_ADMIN')")
    public ResponseEntity<SupplyResponse> updateSupply(@PathVariable Long id,
                                                       @Valid @RequestBody SupplyRequest request) {
        SupplyCommand command = new SupplyCommand(
                request.name(), request.description(), request.quantity(),
                request.unit(), request.category(), request.shelterId(),
                request.userId(), request.supplierName(),
                request.minimumStock(), request.status()
        );
        return supplyService.update(id, command)
                .map(r -> new SupplyResponse(
                        r.id(), r.name(), r.description(), r.quantity(),
                        r.unit(), r.category(), r.shelterId(),
                        r.supplierName(), r.minimumStock(), r.status(),
                        r.createdAt(), r.updatedAt()
                ))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/by-id/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHELTER_ADMIN')")
    public ResponseEntity<Void> deleteSupply(@PathVariable Long id) {
        boolean deleted = supplyService.delete(id);
        return deleted
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
