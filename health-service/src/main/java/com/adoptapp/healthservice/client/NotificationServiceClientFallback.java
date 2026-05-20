package com.adoptapp.healthservice.client;

import com.adoptapp.healthservice.dto.NotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationServiceClientFallback implements NotificationServiceClient {

    @Override
    public ResponseEntity<Void> sendNotification(NotificationRequest request) {
        log.warn("Notificacion no disponible para '{}': {}", request.recipient(), request.message());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
}
