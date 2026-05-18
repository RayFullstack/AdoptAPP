package com.adoptapp.healthservice.client;

import com.adoptapp.healthservice.dto.NotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service", url = "${services.notification-service.url}", fallback = NotificationServiceClientFallback.class)
public interface NotificationServiceClient {

    @PostMapping("/notifications")
    ResponseEntity<Void> sendNotification(@RequestBody NotificationRequest request);
}
