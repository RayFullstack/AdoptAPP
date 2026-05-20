package com.adoptapp.adoptionservice.client;

import com.adoptapp.adoptionservice.dto.UserNotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserNotificationClientFallback implements UserNotificationClient {

    @Override
    public ResponseEntity<Void> sendNotification(UserNotificationRequest request) {
        log.warn("User-notification no disponible para '{}': {}", request.recipient(), request.message());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
}
