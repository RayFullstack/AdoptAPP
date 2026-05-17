package com.adoptapp.petservice.client;

import com.adoptapp.petservice.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${services.user-service.url}", fallback = UserServiceClientFallback.class)
public interface UserServiceClient {

    @GetMapping("/users/by-id/{id}")
    ResponseEntity<UserResponse> getUserById(@PathVariable("id") Long id);

    @GetMapping("/users/by-email/{email}")
    ResponseEntity<UserResponse> getUserByEmail(@PathVariable("email") String email);
}
