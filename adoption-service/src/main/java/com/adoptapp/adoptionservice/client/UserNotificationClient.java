package com.adoptapp.adoptionservice.client;

import com.adoptapp.adoptionservice.dto.UserNotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-notification", url = "${services.notification-service.url}", fallback = UserNotificationClientFallback.class)
public interface UserNotificationClient {

    @PostMapping("/notifications")
    ResponseEntity<Void> sendNotification(@RequestBody UserNotificationRequest request);
}
