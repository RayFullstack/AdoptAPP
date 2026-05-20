package com.adoptapp.staffservice.client;

import com.adoptapp.staffservice.dto.ShelterResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ShelterServiceClientFallback implements ShelterServiceClient {

    @Override
    public ResponseEntity<ShelterResponse> getShelterById(Long id) {
        log.warn("Shelter-service no disponible para shelterId {}", id);
        return ResponseEntity.notFound().build();
    }
}
