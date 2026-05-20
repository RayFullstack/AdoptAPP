package com.adoptapp.notificationservice.client;

import com.adoptapp.sharedkernel.dto.UserAuthResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${services.user-service.url:http://localhost:8081}", fallback = UserServiceClientFallback.class)
public interface UserServiceClient {

    @GetMapping("/users/by-email/{email}/auth")
    ResponseEntity<UserAuthResponse> getUserAuthByEmail(@PathVariable("email") String email);
}
