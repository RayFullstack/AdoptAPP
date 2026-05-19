package com.adoptapp.supplyservice.client;

import com.adoptapp.supplyservice.dto.ShelterResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ShelterServiceClientFallback implements ShelterServiceClient {

    @Override
    public ResponseEntity<ShelterResponse> getShelterById(Long id) {
        log.error("Fallback: No se pudo obtener shelter por id: {}", id);
        return null;
    }
}
