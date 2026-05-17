package com.adoptapp.petservice.client;

import com.adoptapp.petservice.dto.NotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationServiceClientFallback implements NotificationServiceClient {

    @Override
    public ResponseEntity<Void> sendNotification(NotificationRequest request) {
        log.warn("Notification-service no disponible para '{}': tipo={}", request.recipient(), request.typeName());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
}
