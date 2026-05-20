package com.adoptapp.adoptionservice.client;

import com.adoptapp.adoptionservice.dto.PetNotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PetNotificationClientFallback implements PetNotificationClient {

    @Override
    public ResponseEntity<Void> sendNotification(PetNotificationRequest request) {
        log.warn("Pet-notification no disponible para '{}': {}", request.recipient(), request.message());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
}
