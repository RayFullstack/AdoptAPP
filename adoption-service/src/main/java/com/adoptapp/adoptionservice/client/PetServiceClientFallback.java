package com.adoptapp.adoptionservice.client;

import com.adoptapp.adoptionservice.dto.PetResponse;
import com.adoptapp.adoptionservice.dto.PetStatusRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PetServiceClientFallback implements PetServiceClient {

    @Override
    public ResponseEntity<PetResponse> getPetById(Long id) {
        log.warn("Pet-service no disponible para petId {}", id);
        return ResponseEntity.notFound().build();
    }

    @Override
    public ResponseEntity<PetResponse> updatePetStatus(Long id,  PetStatusRequest request) {
        log.warn("Pet-service no disponible para actualizar estado de petId {}", id);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
}
