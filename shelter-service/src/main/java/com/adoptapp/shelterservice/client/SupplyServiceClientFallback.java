package com.adoptapp.shelterservice.client;

import com.adoptapp.shelterservice.dto.SupplyResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SupplyServiceClientFallback implements SupplyServiceClient {

    @Override
    public ResponseEntity<List<SupplyResponse>> getActiveSuppliesByShelter(Long shelterId) {
        return ResponseEntity.status(503).build();
    }
}
