package com.adoptapp.adoptionservice.client;

import com.adoptapp.adoptionservice.dto.PetNotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "pet-notification", url = "${services.notification-service.url}", fallback = PetNotificationClientFallback.class)
public interface PetNotificationClient {

    @PostMapping("/notifications")
    ResponseEntity<Void> sendNotification(@RequestBody PetNotificationRequest request);
}
