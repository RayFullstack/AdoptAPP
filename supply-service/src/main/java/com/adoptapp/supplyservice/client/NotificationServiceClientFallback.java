package com.adoptapp.supplyservice.client;

import com.adoptapp.supplyservice.dto.NotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationServiceClientFallback implements NotificationServiceClient {

    @Override
    public ResponseEntity<Void> sendNotification(NotificationRequest request) {
        log.warn("Fallback: No se pudo enviar notificación a userId={}, type={}",
                request.userId(), request.typeName());
        return null;
    }
}
