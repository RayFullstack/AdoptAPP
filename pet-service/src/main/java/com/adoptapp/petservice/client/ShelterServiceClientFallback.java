package com.adoptapp.petservice.client;

import com.adoptapp.petservice.dto.ShelterResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ShelterServiceClientFallback implements ShelterServiceClient {

    @Override
    public ResponseEntity<ShelterResponse> getShelterById(Long id) {
        log.warn("Shelter-service no disponible para shelterId {}", id);
        return ResponseEntity.notFound().build();
    }
}
