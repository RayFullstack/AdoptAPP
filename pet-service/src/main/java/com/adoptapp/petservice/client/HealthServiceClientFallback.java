package com.adoptapp.petservice.client;

import com.adoptapp.petservice.dto.HealthResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class HealthServiceClientFallback implements HealthServiceClient {

       @Override
    public ResponseEntity<HealthResult> getHealthByPetId(Long id) {
        log.warn("Health-service no disponible para obtener salud ID={}", id);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

    @Override
    public ResponseEntity<Void> deleteHealthByPetId(Long id) {
        log.warn("Health-service no disponible para eliminar salud ID={}", id);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
}
