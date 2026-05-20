package com.adoptapp.healthservice.client;

import com.adoptapp.healthservice.dto.PetResponse;
import lombok.extern.slf4j.Slf4j;
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
}
