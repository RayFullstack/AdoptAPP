package com.adoptapp.followupservice.client;

import com.adoptapp.followupservice.dto.PetResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PetServiceClientFallback implements PetServiceClient {

    @Override
    public ResponseEntity<PetResponse> getPetById(Long id) {
        log.warn("Pet-service no disponible para petId {}", id);
        return ResponseEntity.notFound().build();
    }
}
