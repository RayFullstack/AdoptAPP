package com.adoptapp.shelterservice.client;

import com.adoptapp.shelterservice.dto.PetResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PetServiceClientFallback implements PetServiceClient {

    @Override
    public ResponseEntity<List<PetResponse>> getActivePetsByShelter(Long shelterId) {
        return ResponseEntity.status(503).build();
    }
}
